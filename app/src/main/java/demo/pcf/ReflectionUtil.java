package demo.pcf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionUtil {

	public static Set<String> getColumns(Class<?> klass) {
		Set<String> columns = new TreeSet<>();
		for (Field field : klass.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				columns.add(field.getName());
			}
		}
		return columns;
	}

	public static List<Map<String, String>> getData(Collection<?> collection, Class<?> klass) {
		List<Map<String, String>> data = new ArrayList<>();

		collection.stream().forEach(item -> {
			Map<String, String> datum = new TreeMap<>();

			for (Field field : klass.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					field.setAccessible(true);
					try {
						datum.put(field.getName(), field.get(item).toString());
					} catch (Exception e) {
						log.error("getData", e);
						datum.put(field.getName(), "?");
					}
				}
			}
			data.add(datum);
		});

		return data;
	}

	public static List<Map<String, String>> getData(Iterable<?> iterable, Class<?> klass) {
		List<Object> collection = new ArrayList<>();
		iterable.forEach(collection::add);
		return getData(collection, klass);
	}
}
