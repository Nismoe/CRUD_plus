package com.robert.taskapi.controller;

import com.robert.taskapi.model.Task;
import com.robert.taskapi.repository.TaskRepository;
import com.robert.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskRepository taskRepository;
    private final TaskService taskService;

    // the constructor is used for dependency injection
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //getAllTasks() is mapped to a GET request for the endpoint "/api/tasks".
    // It retrieves a list of all tasks by calling taskService.getAllTasks() and returns that list.
    @GetMapping
    public List<Task> getAllTasks() {
        logger.info("Fetching all tasks");
        List<Task> tasks = taskService.getAllTasks();
        logger.debug("Fetched tasks: {}", tasks);
        return tasks;
    }

    //getTaskById(Long id) is mapped to a GET request for the endpoint "/api/tasks/{id}".
    //It retrieves a task by its ID by calling taskService.getTaskById(id) and returns an Optional<Task>.
    //The @PathVariable annotation is used to extract the id from the URI.
    @GetMapping("/{id}")
    public Optional<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    //This annotation is for handling HTTP POST requests.
    //method creates a new task by calling taskService.createTask(task).
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        logger.info("Created task: {}", createdTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    //This annotation is for handling HTTP PUT requests.
    //method updates an existing task with the specified ID by calling taskService.updateTask(id, task)
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    // PATCH partially update a task by ID
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateTask(@PathVariable Long id, @RequestBody @Valid Task updatedTask) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isPresent()) {
            Task existingTask = optionalTask.get();

            // Update only the fields that are present in the request
            if (updatedTask.getName() != null) {
                existingTask.setName(updatedTask.getName());
            }

            // Save only the updated fields
            existingTask = taskRepository.save(existingTask);

            return ResponseEntity.ok(existingTask);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //This annotation is for handling HTTP DELETE requests.
    //method deletes a task with the specified ID by calling taskService.deleteTask(id).
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headTask(@PathVariable Long id) {
        // Implement as needed
        return ResponseEntity.ok().build();
    }

    // OPTIONS method (just for example, may not be necessary)
    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsTask(@PathVariable Long id) {
        // Implement as needed
        return ResponseEntity.ok().build();
    }
    // Add a method to handle validation errors globally
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errors = new StringBuilder("{");

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            // Append the error message to the StringBuilder
            errors.append("\"").append(fieldName).append("\": \"").append(errorMessage).append("\",");
        });

        // Remove the trailing comma and close the JSON object
        if (errors.charAt(errors.length() - 1) == ',') {
            errors.deleteCharAt(errors.length() - 1);
        }
        errors.append("}");

        // Log the JSON-like error messages
        logger.warn("Validation error: {}", errors.toString());

        // Return the JSON-like error messages as a String
        return errors.toString();
    }
}