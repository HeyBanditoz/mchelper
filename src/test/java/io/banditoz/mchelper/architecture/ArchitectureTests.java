package io.banditoz.mchelper.architecture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import io.avaje.inject.Component;
import io.avaje.inject.spi.Generated;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTests {
    private final JavaClasses classes = new ClassFileImporter().importPackages("io.banditoz.mchelper");

    @Test
    void singletonWithConstructorsShouldHaveInject() {
        constructors()
                .that(haveMoreThanZeroParameters())
                .and().areDeclaredInClassesThat()
                .areAnnotatedWith(Singleton.class)
                .should().beAnnotatedWith(Inject.class)
                .because("this project should be explicit with constructors having injected beans")
                .check(classes);
    }

    @Test
    void componentShouldNotBeUsed() {
        noClasses()
                .should().beAnnotatedWith(Component.class)
                .because("this project prefers Jakarta EE @Singleton")
                .check(classes);
    }

    @Test
    void daoImplShouldNotBeUsedButDaoInstead() {
        noClasses()
                .that().areNotAnnotatedWith(Generated.class) // exclude automatically generated DI classes
                .and().areNotAnnotatedWith(Test.class)
                .and().doNotHaveSimpleName("BaseCommandTest")
                .should().dependOnClassesThat()
                .haveNameMatching(".*DaoImpl")
                .because("classes should depend on DAO interfaces, not implementations")
                .check(classes);
    }

    @Test
    void noObjectMapperCreation() {
        noClasses()
                .that().doNotHaveSimpleName("ObjectMapperFactory")
                .and().areNotAnnotatedWith(Test.class)
                .should().callConstructor(ObjectMapper.class)
                .because("ObjectMapper should be injected")
                .check(classes);
    }

    private DescribedPredicate<JavaConstructor> haveMoreThanZeroParameters() {
        return new DescribedPredicate<>("have more than zero parameters") {
            @Override
            public boolean test(JavaConstructor javaConstructor) {
                return !javaConstructor.getParameters().isEmpty();
            }
        };
    }
}
