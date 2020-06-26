package com.ktully.appd.otel.itemapi.Service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ktully.appd.otel.itemapi.Model.ItemModel;
import com.ktully.appd.otel.itemapi.Repository.ItemRepository;

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

@Service
public class ItemService {

	private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	Tracer tracer;

	public List<ItemModel> getAllItems() {

		// Start a Span for ItemService
		Span itemServiceSpan = tracer.spanBuilder("/item-api/items:getAllItems").setSpanKind(Span.Kind.SERVER)
				.startSpan();
		itemServiceSpan.addEvent("Calling getAllItems()"); // This ends up in "logs" section in Jaeger
		itemServiceSpan.setAttribute("getAllItems-Key", "getAllItems-Value");

		List<ItemModel> items = new ArrayList<ItemModel>();
		itemRepository.findAll().forEach(item -> items.add(item));

		itemServiceSpan.end();

		return items;

	}

	public ItemModel getItemById(int id) {
		return itemRepository.findById(id).get();
	}

	public ItemModel saveOrUpdate(ItemModel itemModel) {
		return itemRepository.save(itemModel);
	}

	public void delete(int id) {
		itemRepository.deleteById(id);
	}

}
