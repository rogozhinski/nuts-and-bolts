package ru.hh.nab.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.hh.nab.testbase.NabTestConfig;

@Configuration
@Import({
    NabTestConfig.class,
    ExampleResource.class
})
public class ExampleTestConfig {
}
