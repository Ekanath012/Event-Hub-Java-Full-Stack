package com.eventhub.dto; import jakarta.validation.constraints.*; public record FeedbackRequest(@Min(1) @Max(5) int rating,@Size(max=2000) String comment){}
