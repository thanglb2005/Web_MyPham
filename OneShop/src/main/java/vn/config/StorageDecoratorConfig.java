package vn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import vn.decorator.storage.FileValidationStorageDecorator;
import vn.decorator.storage.LoggingStorageDecorator;
import vn.service.CloudinaryService;
import vn.service.StorageService;
import vn.service.impl.StorageServiceImpl;

/**
 * Client wiring (role #5): compose decorators around concrete component.
 *
 * Chain:
 *   LoggingStorageDecorator(
 *     FileValidationStorageDecorator(
 *       StorageServiceImpl
 *     )
 *   )
 */
@Configuration
public class StorageDecoratorConfig {

    @Bean
    @Primary
    public StorageService storageServiceDecoratorChain(
            StorageServiceImpl concreteComponent,
            @Autowired(required = false) CloudinaryService cloudinaryService
    ) {
        StorageService validated = new FileValidationStorageDecorator(concreteComponent, cloudinaryService);
        return new LoggingStorageDecorator(validated);
    }
}
