package com.codearena.backend.serviceImpl;


import com.codearena.backend.dto.CodeExecutionDTO;
import com.codearena.backend.dto.CodeExecutionResultDTO; // <-- ADDED
import com.codearena.backend.entity.*;
import com.codearena.backend.repository.CodingQuestionRepository;
import com.codearena.backend.repository.RoomRepository;
import com.codearena.backend.repository.SubmissionRepository;
import com.codearena.backend.repository.TestCaseRepository;
import com.codearena.backend.service.CodeExecutionService;
import com.codearena.backend.service.UserService;
import com.codearena.backend.utils.constant.Status;
import com.codearena.backend.utils.constant.SubmissionStatus;
import org.json.JSONObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private static final String JUDGE0_API_URL =
            "https://ce.judge0.com/submissions?base64_encoded=false&wait=true";
    private final CodingQuestionRepository codingQuestionRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final RoomRepository roomRepository;

    public CodeExecutionServiceImpl(CodingQuestionRepository codingQuestionRepository,
                             TestCaseRepository testCaseRepository,
                             SubmissionRepository submissionRepository,
                             UserService userService,
                             RoomRepository roomRepository) {

        this.codingQuestionRepository = codingQuestionRepository;
        this.testCaseRepository = testCaseRepository;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
        this.roomRepository = roomRepository;
    }


    @Override
    public CodeExecutionResultDTO runCode(CodeExecutionDTO request) {

        CodeExecutionResultDTO result = new CodeExecutionResultDTO();

        result.setExitCode(-1);
        result.setTime(0.0);
        result.setMemory(0.0);

        CodingQuestion codingQuestion =
                codingQuestionRepository.findById(request.getCodingQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        try {

            String language = request.getLanguage();
            String code = request.getCode();
            System.out.println(request);
            // ✅ Fetch FIRST SAMPLE test case
            TestCase sampleTest =
                    testCaseRepository.findFirstByCodingQuestionIdAndIsSampleTrue(
                            codingQuestion.getId()
                    ).orElseThrow(() ->
                            new RuntimeException("Sample test case not found"));

            String stdin;

            if(request.getInput() != null &&
                    !request.getInput().trim().isEmpty()) {

                stdin = request.getInput();

            } else {

                stdin = sampleTest.getInputData();
            }

            // ---------- Build Payload ----------

            JSONObject payload = new JSONObject();

            payload.put("source_code", code);
            payload.put("language_id", getJudge0LanguageId(language));
            payload.put("stdin", stdin);

            // ---------- Call JUDGE0 API ---------- //
            System.out.println(payload.toString());
            HttpResponse<JsonNode> response =
                     Unirest.post(JUDGE0_API_URL)
                            .header("Content-Type", "application/json").header("Accept", "application/json")
                            .body(payload.toString())
                            .asJson();

            kong.unirest.json.JSONObject body =
                    response.getBody().getObject();

            System.out.println("Judge0 Response: " + body.toString(2));


            String stdout = body.isNull("stdout") ? "" : body.optString("stdout", "");
            String stderr = body.isNull("stderr") ? "" : body.optString("stderr", "");
            String compileOutput = body.isNull("compile_output")
                    ? ""
                    : body.optString("compile_output", "");

            kong.unirest.json.JSONObject statusObj =
                    body.optJSONObject("status");

            int statusId = statusObj != null
                    ? statusObj.optInt("id", 0)
                    : 0;

            String statusDescription = statusObj != null
                    ? statusObj.optString("description", "")
                    : "";

            result.setStdout(stdout != null ? stdout.trim() : "");
            result.setStderr(stderr != null ? stderr.trim() : "");
            result.setCompileOutput(
                    compileOutput != null ? compileOutput.trim() : ""
            );

            result.setExitCode(0);

            result.setTime(body.optDouble("time", 0.0));
            result.setMemory(body.optDouble("memory", 0.0));

        /*
              Judge0 Status IDs:
              3 = Accepted
              4 = Wrong Answer
              5 = Time Limit
              6 = Compilation Error
              11 = Runtime Error
        */

            if (!compileOutput.isEmpty()) {

                result.setStdout(
                        "Compilation Error:\n" + compileOutput
                );
            }
            else if (!stderr.isEmpty()) {

                result.setStdout(
                        "Runtime Error:\n" + stderr
                );
            }
            else if (statusId != 3) {

                result.setStdout(
                        "Execution Failed: " + statusDescription
                );
            }
            else {

                result.setStdout(stdout.trim());
            }

            return result;

        } catch (Exception e) {

            e.printStackTrace();
            result.setStderr("Execution error: " + e.getMessage());

            return result;
        }
    }

    @Override
    public CodeExecutionResultDTO submitCode(
            CodeExecutionDTO request,
            int roomCode) {

        CodeExecutionResultDTO result = new CodeExecutionResultDTO();
        User user = userService.getCurrentUser();
        result.setExitCode(-1);
        result.setTime(0.0);
        result.setMemory(0.0);

        // ================= FETCH ROOM =================

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getStatus().equals(Status.ACTIVE)) {
            throw new RuntimeException("Room is not active");
        }

        // ================= FETCH QUESTION =================

        CodingQuestion question =
                codingQuestionRepository.findById(request.getCodingQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        // ================= ATTEMPT NUMBER =================

        int attempt =
                submissionRepository.countByUserIdAndRoomIdAndQuestionId(
                        user.getId(),
                        room.getId(),
                        question.getId()
                ) + 1;

        // ================= FETCH TEST CASES =================

        List<TestCase> testCases =
                testCaseRepository.findByCodingQuestionId(question.getId());

        int passed = 0;
        int total = testCases.size();

        Submission submission = new Submission();

        submission.setUser(user);
        submission.setRoom(room);
        submission.setQuestion(question);
        submission.setLanguage(request.getLanguage());
        submission.setSourceCode(request.getCode());
        submission.setAttemptNumber(attempt);
        submission.setSubmittedAt(LocalDateTime.now());

        // ================= EXECUTION LOOP =================

        try {

            for (TestCase tc : testCases) {

                String stdin = tc.getInputData();

                JSONObject payload = new JSONObject();

                payload.put("source_code", request.getCode());
                payload.put("language_id", getJudge0LanguageId(request.getLanguage()));
                payload.put("stdin", stdin);

                HttpResponse<JsonNode> response =
                        Unirest.post(JUDGE0_API_URL)
                                .header("Content-Type", "application/json")
                                .body(payload.toString())
                                .asJson();

                kong.unirest.json.JSONObject body =
                        response.getBody().getObject();



//                kong.unirest.json.JSONObject run =
//                        body.getJSONObject("run");

                String stderr =
                        body.optString("stderr", "").trim();

                String compileOutput =
                        body.optString("compile_output", "").trim();

                if (!compileOutput.isEmpty()) {

                    submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                    submission.setCompilerMessage(compileOutput);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setCompileOutput(compileOutput);
                    result.setExitCode(1);

                    return result;
                }

                // ================= RUNTIME ERROR =================

                if (!stderr.isEmpty()) {

                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setCompilerMessage(stderr);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setStderr(stderr);
                    result.setExitCode(1);

                    return result;
                }

                String actual =
                        normalize(
                                body.isNull("stdout")
                                        ? ""
                                        : body.optString("stdout", "")
                        );

                String expected =
                        normalize(tc.getExpectedOutput());

                // ================= WRONG ANSWER =================

                if (!actual.equals(expected)) {

                    submission.setStatus(SubmissionStatus.WRONG_ANSWER);
                    submission.setScore(0);

                    // Optional: store judge feedback in DB
                    submission.setCompilerMessage(
                            "TestCase " + tc.getOrderIndex()
                                    + " Failed\nExpected: " + expected
                                    + "\nFound: " + actual
                    );

                    submissionRepository.save(submission);

                    // Send detailed result to frontend
                    result.setStdout(
                            "❌ Wrong Answer at TestCase " + tc.getOrderIndex()
                                    + "\nExpected Output:\n" + expected
                                    + "\n\nYour Output:\n" + actual
                    );

                    result.setExitCode(1);

                    return result;
                }


                // ================= PASS =================

                passed++;

                // Save max execution metrics
                submission.setExecutionTime(
                        Math.max(submission.getExecutionTime(),
                                body.optDouble("time", 0.0)));

                submission.setMemoryUsed(
                        Math.max(submission.getMemoryUsed(),
                                body.optDouble("memory", 0.0)));
            }

            // ================= ACCEPTED =================

            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setScore(question.getPoints());

            submission.setPassedTestCases(passed);
            submission.setTotalTestCases(total);

            submissionRepository.save(submission);

            result.setPassedTestCases(passed);
            result.setTotalTestCases(total);

            result.setStdout("✅ Accepted");
            result.setExitCode(0);

            return result;

        } catch (Exception e) {

            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setCompilerMessage(e.getMessage());
            submission.setScore(0);

            submissionRepository.save(submission);

            result.setStderr("Execution failed");
            return result;
        }
    }


    @Override
    public CodeExecutionResultDTO submitCode(
            CodeExecutionDTO request) {

        CodeExecutionResultDTO result = new CodeExecutionResultDTO();
        User user = userService.getCurrentUser();
        result.setExitCode(-1);
        result.setTime(0.0);
        result.setMemory(0.0);

        // ================= FETCH ROOM =================



        // ================= FETCH QUESTION =================

        CodingQuestion question =
                codingQuestionRepository.findById(request.getCodingQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

        // ================= ATTEMPT NUMBER =================

//        int attempt =
//                submissionRepository.countByUserIdAndRoomIdAndQuestionId(
//                        user.getId(),
//                        room.getId(),
//                        question.getId()
//                ) + 1;

        // ================= FETCH TEST CASES =================

        List<TestCase> testCases =
                testCaseRepository.findByCodingQuestionId(question.getId());

        int passed = 0;
        int total = testCases.size();

        Submission submission = new Submission();

        submission.setUser(user);
//        submission.setRoom(room);
        submission.setQuestion(question);
        submission.setLanguage(request.getLanguage());
        submission.setSourceCode(request.getCode());
//        submission.setAttemptNumber(attempt);
        submission.setSubmittedAt(LocalDateTime.now());

        // ================= EXECUTION LOOP =================

        try {

            for (TestCase tc : testCases) {

                String stdin = tc.getInputData();

                JSONObject payload = new JSONObject();

                payload.put("source_code", request.getCode());
                payload.put("language_id", getJudge0LanguageId(request.getLanguage()));
                payload.put("stdin", stdin);

                HttpResponse<JsonNode> response =
                        Unirest.post(JUDGE0_API_URL)
                                .header("Content-Type", "application/json")
                                .body(payload.toString())
                                .asJson();

                kong.unirest.json.JSONObject body =
                        response.getBody().getObject();




//                kong.unirest.json.JSONObject run =
//                        body.getJSONObject("run");

                String stderr =
                        body.optString("stderr", "").trim();

                String compileOutput =
                        body.optString("compile_output", "").trim();

                if (!compileOutput.isEmpty()) {

                    submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                    submission.setCompilerMessage(compileOutput);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setCompileOutput(compileOutput);
                    result.setExitCode(1);

                    return result;
                }

                // ================= RUNTIME ERROR =================

                if (!stderr.isEmpty()) {

                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setCompilerMessage(stderr);
                    submission.setScore(0);

                    submissionRepository.save(submission);

                    result.setStderr(stderr);
                    result.setExitCode(1);

                    return result;
                }

                String actual =
                        normalize(
                                body.isNull("stdout")
                                        ? ""
                                        : body.optString("stdout", "")
                        );

                String expected =
                        normalize(tc.getExpectedOutput());

                // ================= WRONG ANSWER =================

                if (!actual.equals(expected)) {

                    submission.setStatus(SubmissionStatus.WRONG_ANSWER);
                    submission.setScore(0);

                    // Optional: store judge feedback in DB
                    submission.setCompilerMessage(
                            "TestCase " + tc.getOrderIndex()
                                    + " Failed\nExpected: " + expected
                                    + "\nFound: " + actual
                    );

                    submissionRepository.save(submission);

                    // Send detailed result to frontend
                    result.setStdout(
                            "❌ Wrong Answer at TestCase " + tc.getOrderIndex()
                                    + "\nExpected Output:\n" + expected
                                    + "\n\nYour Output:\n" + actual
                    );

                    result.setExitCode(1);

                    return result;
                }


                // ================= PASS =================

                passed++;

                // Save max execution metrics
                submission.setExecutionTime(
                        Math.max(submission.getExecutionTime(),
                                body.optDouble("time", 0.0)));

                submission.setMemoryUsed(
                        Math.max(submission.getMemoryUsed(),
                                body.optDouble("memory", 0.0)));
            }

            // ================= ACCEPTED =================

            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setScore(question.getPoints());

            submission.setPassedTestCases(passed);
            submission.setTotalTestCases(total);

            submissionRepository.save(submission);

            result.setPassedTestCases(passed);
            result.setTotalTestCases(total);

            result.setStdout("✅ Accepted");
            result.setExitCode(0);

            return result;

        } catch (Exception e) {

            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setCompilerMessage(e.getMessage());
            submission.setScore(0);

            submissionRepository.save(submission);

            result.setStderr("Execution failed");
            return result;
        }
    }
    private int getJudge0LanguageId(String language) {

        return switch (language.toLowerCase()) {
            case "java" -> 62;
            case "python" -> 71;
            case "cpp" -> 54;
            case "javascript" -> 63;
            default -> throw new RuntimeException("Unsupported language");
        };
    }

    private String normalize(String output) {

        return output
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Utility function for file extension
    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "python" -> "py";
            case "java" -> "java";
            case "c" -> "c";
            case "cpp" -> "cpp";
            case "javascript" -> "js";
            case "go" -> "go";
            case "ruby" -> "rb";
            case "php" -> "php";
            default -> "txt";
        };
    }
}