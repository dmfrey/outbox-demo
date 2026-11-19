package com.example.outboxdemo;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/** LESSON 0: module boundaries are enforced by a test, and the docs are generated from the code. */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of( OutboxDemoApplication.class );

    @Test
    void modulesHaveNoCyclesAndRespectEachOthersInternals() {
        modules.verify();
    }

    @Test
    void writeDocumentation() {
        // build/spring-modulith-docs: C4 component diagrams (PlantUML) + a module canvas per module.
        new Documenter( modules ).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml().writeModuleCanvases();
    }

}
