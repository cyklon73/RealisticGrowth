package de.cyklon.realisticgrowth.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class BiList<F, S> extends ArrayList<Map.Entry<Optional<F>, Optional<S>>> implements List<Map.Entry<Optional<F>, Optional<S>>>  {

	public void add(F first, S second) {
		add(Map.entry(Optional.ofNullable(first), Optional.ofNullable(second)));
	}

	public F getFirst(int index) {
		return super.get(index).getKey().orElse(null);
	}

	public S getSecond(int index) {
		return super.get(index).getValue().orElse(null);
	}

	public void forEach(BiConsumer<F, S> consumer) {
		forEach(e -> consumer.accept(e.getKey().orElse(null), e.getValue().orElse(null)));
	}

}
