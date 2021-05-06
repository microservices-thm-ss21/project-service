package de.thm.mni.microservices.gruppe6.project.controller;

import de.thm.mni.microservices.gruppe6.project.model.persistence.Project;
import de.thm.mni.microservices.gruppe6.project.service.ProjectDbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProjectDbService projectDbService;

    private final String PROJECTS_URI = "/api/projects";

    @Test
    public void testShouldReturnEmptyListOfMessages() {
        Iterable<Project> projects = Collections.emptyList();

        given(projectDbService.getAllProjects()).willReturn(Flux.fromIterable(projects));

        webTestClient
                .get()
                .uri(PROJECTS_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$", projects);

        verify(projectDbService, times(1)).getAllProjects();
    }

}
