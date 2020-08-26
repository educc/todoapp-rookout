package com.rookout.tutorial;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.JsonNode;
import io.joshworks.restclient.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bugsnag.Bugsnag;
import io.sentry.Sentry;
import java.util.*;

@RestController
public class TodoController {
    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);
    private TodoStorage todos = TodoStorage.getInstance();

    @RequestMapping(value = "/todos", method = RequestMethod.GET)
    public TodoRecord[] getTodos(@RequestParam(value="from", required=false, defaultValue="") String from) {
        if (from.equalsIgnoreCase("e2e-test")) {
            logger.info("E2E Test just triggered this function");
            return new TodoRecord[0];
        } else if (from.equalsIgnoreCase("statuscake")) {
            logger.info("StatusCake just triggered this function");
            return new TodoRecord[0];
        }
        return todos.getAll();
    }

    @RequestMapping(value = "/todos", method = RequestMethod.POST)
    public ResponseEntity<?> addTodo(@RequestBody TodoRecord newTodoRecord) {
        newTodoRecord.setId(UUID.randomUUID().toString());
        logger.info("Adding a new todo: {}", newTodoRecord);
        // The bug in here in is for the bughunt example
        String todoTitle = newTodoRecord.getTitle().replaceAll("[^a-zA-Z0-9\\s.,!<>]+", "");
        newTodoRecord.setTitle(todoTitle);
        todos.add(newTodoRecord);
        Map<String, String> entities = new HashMap<>();
        entities.put("status", "ok");
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }

    @RequestMapping(value = "/todos", method = RequestMethod.PUT)
    public ResponseEntity<?> updateTodo(@RequestBody TodoRecord updatingTodoRecord) {
        TodoRecord tempTodoRecord = todos.findById(updatingTodoRecord.getId());
        if (tempTodoRecord != null) {
            tempTodoRecord.setTitle(updatingTodoRecord.getTitle());
            tempTodoRecord.setCompleted(updatingTodoRecord.isCompleted());
            logger.info("Updating Todo record: {}", tempTodoRecord);
        }
        Map<String, String> entities = new HashMap<>();
        entities.put("status", "ok");
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }

    @RequestMapping(value = "/todos/{todoId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteTodo(@PathVariable("todoId") String todoId) {
        logger.info("Removing Todo record id: {}", todoId);
        TodoRecord tempTodoRecord = todos.findById(todoId);
        if (tempTodoRecord != null) {
            logger.info("Removing Todo record: {}", tempTodoRecord);
            todos.remove(tempTodoRecord);
        }
        Map<String, String> entities = new HashMap<>();
        entities.put("status", "ok");
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }

    @RequestMapping(value = "/todos/clear_completed", method = RequestMethod.DELETE)
    public ResponseEntity<?> clearCompletedTodos() {
        logger.info("Removing completed todo records");
        for (TodoRecord todoRecord : todos.getAll()) {
            if (todoRecord.isCompleted()) {
                if (todos.remove(todoRecord)) {
                    logger.info("Removing Todo record: {}", todoRecord);
                }
            }
        }
        // Exception Management //
        BugsnagConfig bc = new BugsnagConfig();
        Bugsnag bugsnag = bc.bugsnag();
        if (bugsnag != null) {
            bugsnag.notify(new RuntimeException("Test error"));
        }
        Sentry.capture(new RuntimeException("Test error"));
        // Exception Management //
        Map<String, String> entities = new HashMap<>();
        entities.put("status", "ok");
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }

    @RequestMapping(value = "/todos/dup/{todoId}", method = RequestMethod.POST)
    public ResponseEntity<?> duplicateTodo(@PathVariable("todoId") String todoId) {
        logger.info("Duplicating todo: {}", todoId);
        TodoRecord tempTodoRecord = todos.findById(todoId);
        if (tempTodoRecord != null) {
            TodoRecord newTodoRecord = new TodoRecord(tempTodoRecord);
            newTodoRecord.setId(UUID.randomUUID().toString());
            newTodoRecord.setTitle(tempTodoRecord.getTitle());
            logger.info("Duplicating todo record: {}", newTodoRecord);
            todos.add(newTodoRecord);
        }
        Map<String, String> entities = new HashMap<>();
        entities.put("status", "ok");
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }

    @RequestMapping(value = "/todos/generate/node", method = RequestMethod.POST)
    public ResponseEntity<?> todosGenerateNode() {
        logger.info("Requesting task from node lambda");

        final String env = GetEnv();
        String url;
        if (GetEnv().equals("production")) {
            url = "http://node.task-generator.rookout-demo.com/";
        } else {
            url = "http://" + env + ".node.task-generator.rookout-demo.com/";
        }

        HttpResponse<JsonNode> jsonResponse = Unirest.get(url)
                .header("accept", "application/json")
                .asJson();

        String task = jsonResponse.getBody().getObject().get("task").toString();

        TodoRecord newTodoRecord = new TodoRecord(task,
                                                    UUID.randomUUID().toString(),
                                                    false);
        todos.add(newTodoRecord);

        Map<String, String> entities = new HashMap<>();
        entities.put("status", "ok");
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }

    @RequestMapping(value = "/todos/generate/python", method = RequestMethod.POST)
    public ResponseEntity<?> todosGeneratePython() {
        logger.info("Requesting task from python lambda");

        final String env = GetEnv();
        String url;
        if (GetEnv().equals("production")) {
            url = "http://python.task-generator.rookout-demo.com/";
        } else {
            url = "http://" + env + ".python.task-generator.rookout-demo.com/";
        }

        HttpResponse<JsonNode> jsonResponse = Unirest.get(url)
                .header("accept", "application/json")
                .asJson();

        String task = jsonResponse.getBody().getObject().get("task").toString();

        TodoRecord newTodoRecord = new TodoRecord(task,
                UUID.randomUUID().toString(),
                false);
        todos.add(newTodoRecord);

        Map<String, String> entities = new HashMap<>();
        entities.put("status", "ok");
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }

    private static String GetEnv() {
        String environment = System.getenv("ENV");
        if (environment.isEmpty()) {
            environment = "staging";
        }
        return environment;
    }

    @RequestMapping(value = "/healthz", method = RequestMethod.GET)
    public ResponseEntity<?> healthRoute() {
        return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
    }
}
