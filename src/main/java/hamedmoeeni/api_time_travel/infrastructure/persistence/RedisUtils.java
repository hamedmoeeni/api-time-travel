package hamedmoeeni.api_time_travel.infrastructure.persistence;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.LinkedList;
import java.util.List;

public class RedisUtils {
    public static <T> ReactiveRedisTemplate<String, T> buildRedisTemplate(ReactiveRedisConnectionFactory connectionFactory, Class<T> clazz) {
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

    public static ReactiveRedisTemplate<String, String> buildRedisStringTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();

        RedisSerializationContext.RedisSerializationContextBuilder<String, String> builder = RedisSerializationContext.newSerializationContext();
        RedisSerializationContext<String, String> serializationContext = builder
                .key(keySerializer)
                .value(keySerializer)
                .hashKey(keySerializer)
                .hashValue(keySerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    public static <T> Iterable<T> deserializeSearchResult(Iterable<?> rawResult, RedisSerializer<T> serializer) {
        List<T> result = new LinkedList<>();
        rawResult.forEach(o -> {
            if (o instanceof List) {
                ((Iterable<?>) o).forEach(e -> {
                    if (e instanceof String && ((String) e).startsWith("{")) {
                        result.add(serializer.deserialize(((String) e).getBytes()));
                    }
                });
            }
        });
        return result;
    }
}
