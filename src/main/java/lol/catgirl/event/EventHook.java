package lol.catgirl.event;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) 
public @interface EventHook {
    EventPriority priority() default EventPriority.NORMAL;
}