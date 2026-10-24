package com.worklyn.modules.rewind.planner;

import com.worklyn.modules.rewind.model.WorkflowStep;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates DAG acyclicity and structural integrity using Kahn's topological sort algorithm.
 */
@Component
public class DependencyValidator {

    /**
     * Validates the workflow steps DAG.
     *
     * @param steps list of workflow steps
     * @return topologically sorted list of steps
     * @throws IllegalArgumentException if steps is empty, contains duplicates, missing dependencies, or self-dependencies
     * @throws IllegalStateException    if a circular dependency (cycle) is detected
     */
    public List<WorkflowStep> validateDAG(List<WorkflowStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Workflow steps must not be null or empty");
        }

        Map<String, WorkflowStep> stepMap = new HashMap<>();
        Map<String, Set<String>> outgoingEdges = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for (WorkflowStep step : steps) {
            String stepId = step.getStepId();
            if (stepId == null || stepId.isBlank()) {
                throw new IllegalArgumentException("Step ID must not be null or blank");
            }
            if (stepMap.containsKey(stepId)) {
                throw new IllegalArgumentException("Duplicate step ID detected: " + stepId);
            }
            stepMap.put(stepId, step);
            outgoingEdges.put(stepId, new HashSet<>());
            inDegree.put(stepId, 0);
        }

        for (WorkflowStep step : steps) {
            String stepId = step.getStepId();
            List<String> deps = step.getDependsOnStepIds();
            if (deps != null) {
                for (String depId : deps) {
                    if (depId == null || depId.isBlank()) {
                        throw new IllegalArgumentException("Dependency step ID must not be blank in step: " + stepId);
                    }
                    if (depId.equals(stepId)) {
                        throw new IllegalArgumentException("Self-dependency detected for step: " + stepId);
                    }
                    if (!stepMap.containsKey(depId)) {
                        throw new IllegalArgumentException("Missing dependency step [" + depId + "] referenced by step [" + stepId + "]");
                    }
                    outgoingEdges.get(depId).add(stepId);
                    inDegree.put(stepId, inDegree.get(stepId) + 1);
                }
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<WorkflowStep> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String u = queue.poll();
            sorted.add(stepMap.get(u));

            for (String v : outgoingEdges.get(u)) {
                int newInDegree = inDegree.get(v) - 1;
                inDegree.put(v, newInDegree);
                if (newInDegree == 0) {
                    queue.offer(v);
                }
            }
        }

        if (sorted.size() != steps.size()) {
            List<String> remainingCycleSteps = inDegree.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .toList();
            throw new IllegalStateException("Circular dependency detected involving steps: " + remainingCycleSteps);
        }

        return sorted;
    }
}
