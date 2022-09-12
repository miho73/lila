package com.github.miho73.lila.controllers;

import com.github.miho73.lila.objects.Exception.LiLACParsingException;
import com.github.miho73.lila.objects.Problem;
import com.github.miho73.lila.services.ProblemService;
import com.github.miho73.lila.services.SessionService;
import com.github.miho73.lila.utils.LiLACRenderer;
import com.github.miho73.lila.utils.RestfulResponse;
import com.github.miho73.lila.utils.Verifiers;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Controller("ProblemController")
@RequestMapping("/problems")
public class ProblemController {

    @Autowired
    SessionService sessionService;

    @Autowired
    ProblemService problemService;

    @GetMapping("")
    public String problem(Model model, HttpSession session) {
        sessionService.loadIdentity(model, session);
        model.addAttribute("page", 0);
        return "problem/problemList";
    }


    @GetMapping("/create")
    public String createProblem(Model model, HttpSession session) {
        sessionService.loadIdentity(model, session);
        model.addAttribute("newProblem", true);
        return "problem/problemSettings";
    }
    @PostMapping(
            value = "/create",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseBody
    public String createProblemPost(HttpServletResponse response, @RequestBody Map<String, Object> requestBody) {
        try {
            Problem problem = new Problem();
            problem.setName(requestBody.get("problem_name").toString());
            problem.setTag((int)requestBody.get("tags"));
            problem.setBranch((int)requestBody.get("branch"));
            problem.setDifficulty((int)requestBody.get("difficulty"));
            problem.setContent(requestBody.get("content").toString());
            problem.setSolution(requestBody.get("solution").toString());
            problem.setStatus((int)requestBody.get("state"));
            problem.setAnswer(requestBody.get("answer").toString());


            // Verify parameters
            if(!Verifiers.inRange(problem.getName().length(), 50, 1)) {
                log.warn("failed to create problem: problem name length out of bound");
                response.setStatus(400);
                return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Problem name has illegal length");
            }
            problem.setName(problem.getName().replace("<", "&lt;").replace(">", "&gt;"));

            problemService.createProblem(problem);
        } catch (NullPointerException e) {
            log.error("failed to create problem", e);
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Missing parameter(s)");
        } catch (ClassCastException e) {
            log.warn("failed to create problem", e);
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Unexpected parameter type");
        } catch (IllegalStateException e) {
            log.warn("failed to create problem", e);
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Illegal state for branch and/or difficulty");
        } catch (SQLException e) {
            log.error("failed to create problem", e);
            response.setStatus(500);
            return RestfulResponse.responseMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        } catch (LiLACParsingException e) {
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "LiLAC cannot be compiled: "+e.getMessage());
        } catch (Exception e) {
            log.error("failed to create problem", e);
            response.setStatus(500);
            return RestfulResponse.responseResult(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
        }
        return RestfulResponse.responseResult(HttpStatus.CREATED, problemService.PROBLEM_COUNT);
    }


    @GetMapping("/update/{problem_code}")
    public String updateProblem(Model model, HttpSession session, HttpServletResponse response,
                                @PathVariable("problem_code") int problem_code) throws Exception {
        sessionService.loadIdentity(model, session);
        model.addAttribute("newProblem", false);
        model.addAttribute("problemCode", problem_code);
        return "problem/problemSettings";
    }
    @PutMapping(
            value = "/update/{problem_code}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseBody
    public String updateProblemPost(HttpServletResponse response,
                                    @PathVariable("problem_code") int problem_code,
                                    @RequestBody Map<String, Object> requestBody) {
        try {
            Problem problem = new Problem();
            problem.setName(requestBody.get("problem_name").toString());
            problem.setTag((int)requestBody.get("tags"));
            problem.setBranch((int)requestBody.get("branch"));

            problem.setDifficulty((int)requestBody.get("difficulty"));
            problem.setContent(requestBody.get("content").toString());
            problem.setSolution(requestBody.get("solution").toString());
            problem.setStatus((int)requestBody.get("state"));
            problem.setAnswer(requestBody.get("answer").toString());


            // Verify parameters
            if(!Verifiers.inRange(problem.getName().length(), 50, 1)) {
                log.warn("failed to update problem code "+problem_code+": problem name length out of bound");
                response.setStatus(400);
                return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Problem name has illegal length");
            }
            problem.setName(problem.getName().replace("<", "&lt;").replace(">", "&gt;"));

            problemService.updateProblem(problem_code, problem);
        } catch (NullPointerException e) {
            log.error("failed to update problem", e);
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Missing parameter(s)");
        } catch (ClassCastException e) {
            log.warn("failed to update problem", e);
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Unexpected parameter type");
        } catch (IllegalStateException e) {
            log.warn("failed to update problem", e);
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Illegal state for branch and/or difficulty");
        } catch (SQLException e) {
            log.error("failed to update problem", e);
            response.setStatus(500);
            return RestfulResponse.responseMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        } catch (LiLACParsingException e) {
            response.setStatus(400);
            return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "LiLAC cannot be compiled: "+e.getMessage());
        } catch (Exception e) {
            log.error("failed to update problem", e);
            response.setStatus(500);
            return RestfulResponse.responseResult(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
        }
        return RestfulResponse.responseResult(HttpStatus.OK, problem_code);
    }


    @GetMapping("/solve/{problem_code}")
    public String solveProblem(Model model, HttpSession session, HttpServletResponse response,
                               @PathVariable("problem_code") int problem_code) throws Exception {
        sessionService.loadIdentity(model, session);
        Problem problem = problemService.getProblem(problem_code);
        if(problem == null) {
            response.sendError(404);
            return null;
        }
        int tagsBit = problem.getTag();
        boolean[] tags = new boolean[32];
        Arrays.fill(tags, false);
        for (int i = 0; i < tags.length; i++)
            if ((tagsBit & 1 << i) != 0)
                tags[i] = true;
        model.addAllAttributes(Map.of(
                "code", problem.getCode(),
                "name", problem.getName(),
                "content", problem.getHtmlContent(),
                "solution", problem.getHtmlSolution(),
                "active", problem.getStatusCode(),
                "tags", tags
        ));
        return "problem/problemPage";
    }


    @GetMapping(value = "/get",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public String getProblem(HttpSession session,
                             HttpServletResponse response,
                             @RequestParam("problem-code") int problem_code) {
        try {
            Problem problem = problemService.getProblem(problem_code);
            if(problem == null) {
                return RestfulResponse.responseResult(HttpStatus.NOT_FOUND, "no problem with code "+problem_code+" was found");
            }
            JSONObject resp = new JSONObject();
            resp.put("code", problem.getCode());
            resp.put("name", problem.getName());
            resp.put("content", problem.getContent());
            resp.put("solution", problem.getSolution());
            resp.put("branch", problem.getBranchCode());
            resp.put("difficulty", problem.getDifficultyCode());
            resp.put("state", problem.getStatusCode());
            resp.put("tags", problem.getTag());
            return RestfulResponse.responseResult(HttpStatus.OK, resp);
        } catch (Exception e) {
            response.setStatus(500);
            return RestfulResponse.responseResult(HttpStatus.INTERNAL_SERVER_ERROR, "Database error.");
        }
    }
    @PostMapping(
            value = "/lilac/compile",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public String compileLilac(HttpServletResponse response, @RequestBody Map<String,String> requestBody) {
        try {
            if(requestBody.get("lilac") == null) {
                response.setStatus(400);
                return RestfulResponse.responseMessage(HttpStatus.BAD_REQUEST, "Given LiLAC code is null");
            }
            String html = LiLACRenderer.render(requestBody.get("lilac"));
            return RestfulResponse.responseResult(HttpStatus.OK, html);
        } catch (LiLACParsingException e) {
            response.setStatus(500);
            return RestfulResponse.responseMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @GetMapping("lilac/preview")
    public String previewLilac() {
        return "problem/lilacPreview";
    }
}
