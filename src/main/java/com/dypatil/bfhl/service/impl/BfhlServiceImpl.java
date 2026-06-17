package com.dypatil.bfhl.service.impl;

import com.dypatil.bfhl.dto.BfhlRequest;
import com.dypatil.bfhl.dto.BfhlResponse;
import com.dypatil.bfhl.service.BfhlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of BfhlService.
 * Processes mixed input arrays and categorizes elements into:
 * numbers (odd/even), alphabets, and special characters.
 * Also handles alphanumeric strings by extracting sub-components.
 */
@Slf4j
@Service
public class BfhlServiceImpl implements BfhlService {

    private static final Set<Character> VOWELS = Set.of('A', 'E', 'I', 'O', 'U', 'a', 'e', 'i', 'o', 'u');
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern PURE_NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern PURE_ALPHA_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern PURE_SPECIAL_PATTERN = Pattern.compile("^[^a-zA-Z0-9]+$");
    private static final int ASYNC_THRESHOLD = 10_000;

    @Override
    public BfhlResponse process(BfhlRequest request, String requestId) {
        long startTime = System.currentTimeMillis();
        log.info("Processing request: requestId={}, dataSize={}",
                requestId, request.getData() != null ? request.getData().size() : 0);

        BfhlResponse response = doProcess(request, requestId, null, startTime);

        log.info("Completed request: requestId={}, processingTimeMs={}",
                requestId, response.getProcessingTimeMs());
        return response;
    }

    @Override
    @Async
    public CompletableFuture<BfhlResponse> processAsync(BfhlRequest request, String requestId, String correlationId) {
        long startTime = System.currentTimeMillis();
        log.info("Processing async request: requestId={}, correlationId={}, dataSize={}",
                requestId, correlationId, request.getData() != null ? request.getData().size() : 0);

        BfhlResponse response = doProcess(request, requestId, correlationId, startTime);

        log.info("Completed async request: requestId={}, correlationId={}, processingTimeMs={}",
                requestId, correlationId, response.getProcessingTimeMs());
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Core processing logic shared by both sync and async paths.
     */
    private BfhlResponse doProcess(BfhlRequest request, String requestId, String correlationId, long startTime) {
        List<String> rawData = request.getData() != null ? request.getData() : Collections.emptyList();
        int totalReceived = rawData.size();

        // Step 1: Filter out nulls, empty strings, and whitespace-only strings
        List<String> validRaw = rawData.stream()
                .filter(this::isValid)
                .collect(Collectors.toList());

        int invalidCount = totalReceived - validRaw.size();

        // Step 2: Detect duplicates before deduplication
        boolean containsDuplicates = hasDuplicates(validRaw);
        int uniqueElementCount = (int) validRaw.stream().distinct().count();

        // Step 3: Deduplicate
        List<String> deduplicated = validRaw.stream().distinct().collect(Collectors.toList());

        // Step 4: Classify and collect
        List<BigDecimal> numericValues = new ArrayList<>();
        List<String> oddNumbers = new ArrayList<>();
        List<String> evenNumbers = new ArrayList<>();
        List<String> alphabets = new ArrayList<>();
        List<String> specialCharacters = new ArrayList<>();
        Map<Character, Integer> charFrequency = new LinkedHashMap<>();

        // Track pure alphabetic strings for longest/shortest
        List<String> pureAlphaStrings = new ArrayList<>();

        for (String token : deduplicated) {
            if (PURE_NUMBER_PATTERN.matcher(token).matches()) {
                // Pure number
                BigDecimal num = new BigDecimal(token);
                numericValues.add(num);
                classifyNumber(num, token, oddNumbers, evenNumbers);

            } else if (PURE_ALPHA_PATTERN.matcher(token).matches()) {
                // Pure alphabetic
                alphabets.addAll(extractAlphabet(token));
                pureAlphaStrings.add(token);
                updateCharFrequency(token, charFrequency);

            } else if (PURE_SPECIAL_PATTERN.matcher(token).matches()) {
                // Pure special characters
                for (char c : token.toCharArray()) {
                    specialCharacters.add(String.valueOf(c));
                }

            } else {
                // Alphanumeric: extract numbers and alphabets separately
                extractAlphanumeric(token, numericValues, oddNumbers, evenNumbers, alphabets,
                        specialCharacters, charFrequency);
            }
        }

        // Step 5: Compute derived fields
        String sum = computeSum(numericValues);
        String largestNumber = numericValues.isEmpty() ? null :
                numericValues.stream().max(BigDecimal::compareTo).map(this::formatNumber).orElse(null);
        String smallestNumber = numericValues.isEmpty() ? null :
                numericValues.stream().min(BigDecimal::compareTo).map(this::formatNumber).orElse(null);

        List<String> sortedNumbers = numericValues.stream()
                .sorted()
                .map(this::formatNumber)
                .collect(Collectors.toList());

        int vowelCount = countVowels(alphabets);

        // Alphabet frequency as String keys
        Map<String, Integer> alphabetFrequency = new LinkedHashMap<>();
        charFrequency.forEach((k, v) -> alphabetFrequency.put(String.valueOf(Character.toUpperCase(k)), v));

        String longestAlpha = pureAlphaStrings.stream()
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
        String shortestAlpha = pureAlphaStrings.stream()
                .min(Comparator.comparingInt(String::length))
                .orElse(null);

        long processingTimeMs = System.currentTimeMillis() - startTime;

        BfhlResponse.Summary summary = BfhlResponse.Summary.builder()
                .totalElementsReceived(totalReceived)
                .validElementsProcessed(deduplicated.size())
                .invalidElementsIgnored(invalidCount)
                .build();

        return BfhlResponse.builder()
                .isSuccess(true)
                .requestId(requestId)
                .correlationId(correlationId)
                .oddNumbers(oddNumbers)
                .evenNumbers(evenNumbers)
                .alphabets(alphabets)
                .specialCharacters(specialCharacters)
                .sum(sum)
                .largestNumber(largestNumber)
                .smallestNumber(smallestNumber)
                .alphabetCount(alphabets.size())
                .numberCount(numericValues.size())
                .specialCharacterCount(specialCharacters.size())
                .containsDuplicates(containsDuplicates)
                .uniqueElementCount(uniqueElementCount)
                .processingTimeMs(processingTimeMs)
                .alphabetFrequency(alphabetFrequency.isEmpty() ? null : alphabetFrequency)
                .sortedNumbers(sortedNumbers.isEmpty() ? null : sortedNumbers)
                .vowelCount(vowelCount)
                .longestAlphabeticValue(longestAlpha)
                .shortestAlphabeticValue(shortestAlpha)
                .summary(summary)
                .build();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasDuplicates(List<String> list) {
        Set<String> seen = new HashSet<>();
        for (String item : list) {
            if (!seen.add(item)) return true;
        }
        return false;
    }

    private void classifyNumber(BigDecimal num, String original, List<String> odd, List<String> even) {
        // For integers: classify as odd/even. Decimals go to neither list but are counted.
        if (num.stripTrailingZeros().scale() <= 0) {
            // Integer-valued
            if (num.toBigIntegerExact().abs().mod(java.math.BigInteger.TWO).equals(java.math.BigInteger.ZERO)) {
                even.add(original);
            } else {
                odd.add(original);
            }
        }
        // Decimals: counted in numericValues but not in odd/even lists
    }

    private List<String> extractAlphabet(String token) {
        List<String> result = new ArrayList<>();
        for (char c : token.toCharArray()) {
            if (Character.isLetter(c)) {
                result.add(String.valueOf(c).toUpperCase());
            }
        }
        return result;
    }

    private void updateCharFrequency(String token, Map<Character, Integer> freq) {
        for (char c : token.toCharArray()) {
            if (Character.isLetter(c)) {
                char upper = Character.toUpperCase(c);
                freq.merge(upper, 1, Integer::sum);
            }
        }
    }

    private void extractAlphanumeric(String token,
                                     List<BigDecimal> numericValues,
                                     List<String> oddNumbers,
                                     List<String> evenNumbers,
                                     List<String> alphabets,
                                     List<String> specialCharacters,
                                     Map<Character, Integer> charFrequency) {
        // Extract all numbers from the alphanumeric token
        java.util.regex.Matcher numMatcher = NUMBER_PATTERN.matcher(token);
        while (numMatcher.find()) {
            String numStr = numMatcher.group();
            BigDecimal num = new BigDecimal(numStr);
            numericValues.add(num);
            classifyNumber(num, numStr, oddNumbers, evenNumbers);
        }

        // Extract all alphabets
        for (char c : token.toCharArray()) {
            if (Character.isLetter(c)) {
                String upper = String.valueOf(c).toUpperCase();
                alphabets.add(upper);
                char upperChar = Character.toUpperCase(c);
                charFrequency.merge(upperChar, 1, Integer::sum);
            } else if (!Character.isDigit(c) && c != '-' && c != '.') {
                // Special character within alphanumeric
                specialCharacters.add(String.valueOf(c));
            }
        }
    }

    private String computeSum(List<BigDecimal> numbers) {
        if (numbers.isEmpty()) return "0";
        BigDecimal total = numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return formatNumber(total);
    }

    private String formatNumber(BigDecimal num) {
        // Remove trailing zeros for cleaner output
        BigDecimal stripped = num.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return stripped.toPlainString();
        }
        return stripped.toPlainString();
    }

    private int countVowels(List<String> alphabetChars) {
        return (int) alphabetChars.stream()
                .flatMapToInt(s -> s.chars())
                .filter(c -> VOWELS.contains((char) c))
                .count();
    }
}
