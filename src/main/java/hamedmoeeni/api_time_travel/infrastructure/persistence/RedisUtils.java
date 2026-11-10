package hamedmoeeni.api_time_travel.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    public static <T> Page<T> deserializeSearchResult(Iterable<?> rawResult, RedisSerializer<T> serializer, Pageable pageable) {
        List<T> result = new LinkedList<>();
        Long[] total = new Long[1];
        rawResult.forEach(l1 -> {
            if (l1 instanceof List) {
                ((Iterable<?>) l1).forEach(l2 -> {
                    //First item is the total number of elements
                    if (total[0] == null) {
                        if (l2 instanceof Long) {
                            total[0] = (Long) l2;
                        }
                    }
                    if (l2 instanceof List) {
                        ((Iterable<?>) l2).forEach(l3 -> {
                            if (l3 instanceof String && ((String) l3).startsWith("{")) {
                                result.add(serializer.deserialize(((String) l3).getBytes()));
                            }
                        });
                    }
                });
            }
        });
        return new PageImpl<>(result, pageable, total[0]);
    }
}
