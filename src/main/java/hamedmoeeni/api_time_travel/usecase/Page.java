package hamedmoeeni.api_time_travel.usecase;

import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public class Page<T> {
    private final List<T> content;
    private final Long total;
    private final Long offset;

    public Page(List<T> content, Long total, Long offset) {
        this.content = content;
        this.total = total;
        this.offset = offset;
    }

    public Page(List<T> content) {
        this(content, content == null ? 0L : content.size(), 0L);
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(this.content.stream().map(mapper).toList(), this.total, this.offset);
    }
}
