package com.theodo.inspector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.theodo.tools.preauthorize.analyzer.PreAuthorizeAnalysis;
import com.theodo.tools.preauthorize.analyzer.impl.utils.AnnotationDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class SpringAccessInspectorTest extends UnitTest {
    private static List<AnnotationDto> annotations;

    @BeforeAll
    static void setupProjects() {
        try {
            PreAuthorizeAnalysis analysis = new PreAuthorizeAnalysis();
            analysis.setProjectDirectory(sampleProjectRootPath);
            annotations = analysis.analyzer();
        } catch (Exception e) {
            log.info("Error while analyzing project");
            e.printStackTrace();
        }

    }

    @Test
    void testRolesAllowed() {

        assertEquals(List.of(
                "GetMapping /webclient/v2/cars 🚨 No PreAuthorize annotation found",
                "GetMapping /webclient/v3/cars/{param}/details ROLE_USER",
                "PostMapping /webclient/v1/cars ROLE_ADMIN"),
                getAnnotationsForEndpoint(annotations, "cars"));

    }

    @Test
    void testPreAuthorizeWithDefaultFunctions() {

        assertEquals(List.of(
                "GetMapping /houses/v1/house isAuthenticated()",
                "PostMapping /houses/v1/house permitAll()"),
                getAnnotationsForEndpoint(annotations, "houses"));

    }

    @Test
    void testPreAuthorizeOnController() {

        assertEquals(List.of(
                "GetMapping /trees hasRole(USER)",
                "PostMapping /trees hasRole(ADMIN)"),
                getAnnotationsForEndpoint(annotations, "trees"));

    }

    @Test
    void testSecured() {

        assertEquals(List.of(
                "DeleteMapping /another/delete ROLE_ADMIN",
                "DeleteMapping /another/{param}/{param} ROLE_ADMIN",
                "PutMapping /another/upload ROLE_USER",
                "PutMapping /another/{param} ROLE_USER",
                "RequestMapping /another/modify 🚨 No PreAuthorize annotation found",
                "RequestMapping /another/{param} 🚨 No PreAuthorize annotation found"),
                getAnnotationsForEndpoint(annotations, "another"));

    }

    private List<String> getAnnotationsForEndpoint(List<AnnotationDto> annotations, String endpoint) {
        List<String> filteredAnnotations = new ArrayList<>();

        annotations.stream().filter(annotation -> annotation.endpoint().contains(endpoint))
                .map(annotation -> "%s %s %s".formatted(annotation.method(), annotation.endpoint(),
                        annotation.preAuthorize()))
                .forEach(filteredAnnotations::add);
        filteredAnnotations.sort(Comparator.naturalOrder());

        return filteredAnnotations;
    }
}
