package com.worklyn.modules.rewind.planner;

import com.worklyn.modules.rewind.model.StepActionType;
import com.worklyn.modules.rewind.model.StepStatus;
import com.worklyn.modules.rewind.model.WorkflowStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyValidatorTest {

    private DependencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DependencyValidator();
    }

    @Test
    @DisplayName("Valid linear DAG returns correct topological order")
    void testValidLinearDAG() {
        WorkflowStep step1 = WorkflowStep.builder().stepId("STEP_1").build();
        WorkflowStep step2 = WorkflowStep.builder().stepId("STEP_2").dependsOnStepIds(List.of("STEP_1")).build();
        WorkflowStep step3 = WorkflowStep.builder().stepId("STEP_3").dependsOnStepIds(List.of("STEP_2")).build();

        List<WorkflowStep> sorted = validator.validateDAG(Arrays.asList(step3, step1, step2));
        assertEquals(3, sorted.size());
        assertEquals("STEP_1", sorted.get(0).getStepId());
        assertEquals("STEP_2", sorted.get(1).getStepId());
        assertEquals("STEP_3", sorted.get(2).getStepId());
    }

    @Test
    @DisplayName("Direct cycle (A -> B -> A) is rejected with IllegalStateException")
    void testDirectCycleDetection() {
        WorkflowStep a = WorkflowStep.builder().stepId("A").dependsOnStepIds(List.of("B")).build();
        WorkflowStep b = WorkflowStep.builder().stepId("B").dependsOnStepIds(List.of("A")).build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validateDAG(Arrays.asList(a, b)));
        assertTrue(ex.getMessage().contains("Circular dependency"));
    }

    @Test
    @DisplayName("Indirect multi-step cycle (A -> B -> C -> A) is rejected")
    void testIndirectCycleDetection() {
        WorkflowStep a = WorkflowStep.builder().stepId("A").dependsOnStepIds(List.of("C")).build();
        WorkflowStep b = WorkflowStep.builder().stepId("B").dependsOnStepIds(List.of("A")).build();
        WorkflowStep c = WorkflowStep.builder().stepId("C").dependsOnStepIds(List.of("B")).build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validateDAG(Arrays.asList(a, b, c)));
        assertTrue(ex.getMessage().contains("Circular dependency"));
    }

    @Test
    @DisplayName("Self-dependency is rejected with IllegalArgumentException")
    void testSelfDependency() {
        WorkflowStep step = WorkflowStep.builder().stepId("SELF").dependsOnStepIds(List.of("SELF")).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validateDAG(List.of(step)));
        assertTrue(ex.getMessage().contains("Self-dependency"));
    }

    @Test
    @DisplayName("Missing dependency is rejected with IllegalArgumentException")
    void testMissingDependency() {
        WorkflowStep step = WorkflowStep.builder().stepId("STEP_X").dependsOnStepIds(List.of("NON_EXISTENT")).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validateDAG(List.of(step)));
        assertTrue(ex.getMessage().contains("Missing dependency"));
    }

    @Test
    @DisplayName("Duplicate step IDs are rejected")
    void testDuplicateStepIds() {
        WorkflowStep s1 = WorkflowStep.builder().stepId("STEP_1").build();
        WorkflowStep s2 = WorkflowStep.builder().stepId("STEP_1").build();

        assertThrows(IllegalArgumentException.class, () -> validator.validateDAG(Arrays.asList(s1, s2)));
    }
}
