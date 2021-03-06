package com.ktully.appd.otel.itemapi.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ktully.appd.otel.itemapi.OtelTracerConfig;
import com.ktully.appd.otel.itemapi.Model.ItemModel;
import com.ktully.appd.otel.itemapi.Repository.ItemRepository;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

@Service
public class ItemService {

	@Autowired
	ItemRepository itemRepository;

	//@Autowired
	//Tracer tracer;
	
	//@Autowired
	//static OpenTelemetry openTelemetry;
	private static final OpenTelemetry openTelemetry = OtelTracerConfig.OpenTelemetryConfig();
	
	private static final Tracer tracer =
		      openTelemetry.getTracer("com.ktully.appd.otel.itemapi");

	public List<ItemModel> getAllItems() {

		// Start a Span for ItemService
		Span itemServiceSpan = tracer.spanBuilder("/item-api/items:getAllItems").setSpanKind(SpanKind.SERVER)
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
