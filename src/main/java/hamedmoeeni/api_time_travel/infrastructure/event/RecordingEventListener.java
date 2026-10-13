package hamedmoeeni.api_time_travel.infrastructure.event;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RecordingEventListener {
    private final hamedmoeeni.api_time_travel.adapter.event.recording.RecordingEventListener recordingEventListener;

    public RecordingEventListener(hamedmoeeni.api_time_travel.adapter.event.recording.RecordingEventListener recordingEventListener) {
        this.recordingEventListener = recordingEventListener;
    }

    @EventListener(Recording.class)
    public void onStatsEvent(Recording recording){
        this.recordingEventListener.onStats(recording).subscribe();
    }
}
