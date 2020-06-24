package com.ktully.appd.otel.itemapi.Repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ktully.appd.otel.itemapi.Model.ItemModel;

@Repository
public interface ItemRepository extends CrudRepository<ItemModel, Integer>{

}
