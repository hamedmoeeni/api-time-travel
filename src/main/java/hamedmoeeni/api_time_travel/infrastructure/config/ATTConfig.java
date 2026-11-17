package hamedmoeeni.api_time_travel.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("att")
@Data
@SuppressWarnings("unused")
public class ATTConfig {
    private Management management;
    private PersistenceType persistenceType;
    private String persistenceEntitiesPrefix;
}
