package hamedmoeeni.api_time_travel.infrastructure.persistence;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisUtils {
    public  static  <T> ReactiveRedisTemplate<String, T> buildRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,Class<T> clazz) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        RedisSerializer<T> valueSerializer = new JacksonJsonRedisSerializer<>(clazz);

        RedisSerializationContext.RedisSerializationContextBuilder<String, T> builder = RedisSerializationContext.newSerializationContext();
        RedisSerializationContext<String, T> serializationContext = builder
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
