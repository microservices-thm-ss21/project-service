package de.thm.mni.microservices.gruppe6.project.service;

import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository;
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProjectDbServiceTest {

    private ProjectDbService projectDbService;

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    public void setUp() {
        projectDbService = new ProjectDbService(projectRepository, memberRepository);
    }

    @Test
    public void testShouldReturnEmptyListOfProjects() {
        given(projectRepository.findAll()).willReturn(Flux.fromIterable(Collections.emptyList()));
        assertThat(projectDbService.getAllProjects().collectList().block()).as("Empty project list").isEqualTo(Collections.emptyList());
        verify(projectRepository, times(1)).findAll();
    }

}
