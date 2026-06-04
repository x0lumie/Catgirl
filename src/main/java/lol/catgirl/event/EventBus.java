package lol.catgirl.event;

import lol.catgirl.Catgirl;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class EventBus {
    private final Map<Class<?>, List<Listener>> listeners = new HashMap<>();

    public void subscribe(@NonNull Object... objs) {
        for (final var o : objs) {
            for (final var method : o.getClass().getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EventHook.class))
                    continue;

                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) continue;

                Class<?> eventType = params[0];
                method.setAccessible(true);

                EventHook hook = method.getAnnotation(EventHook.class);
                Listener listener = new Listener(o, method, hook.priority());
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                        .add(listener);

                listeners.get(eventType)
                        .sort(Comparator.comparingInt(l -> l.priority().ordinal()));
            }
        }
    }

    public void unsubscribe(Object obj) {
        for(List<Listener> group : listeners.values()) {
            group.removeIf(listener -> listener.target == obj);
        }
    }

    public void post(Object event) {
        final var list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) return;

        final var listenersCopy = new ArrayList<>(list);

        for (final var l : listenersCopy) {
            try {
                l.method.invoke(l.target, event);
            } catch (InvocationTargetException e) {
                Catgirl.LOGGER.error("Event handler exception in " + l.target.getClass().getName() + "#" + l.method.getName() + " -> " + e.getCause());
                e.getCause().printStackTrace();
            } catch (IllegalAccessException e) {
                Catgirl.LOGGER.error("IllegalAccessException from invoking method: {}"+ l.method);
            } catch (Exception e) {
                Catgirl.LOGGER.error("Failed to invoke event handler:"+ e);
            }
        }
    }

    private record Listener(Object target, Method method, EventPriority priority) {}
}