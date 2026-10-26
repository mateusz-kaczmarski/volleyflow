package pl.volleyflow.match.model.set;

import jakarta.validation.constraints.Size;

public record SetVideoUpdateRequest(@Size(max = 2048) String videoUrl) {
}
