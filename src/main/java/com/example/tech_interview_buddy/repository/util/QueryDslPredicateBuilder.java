package com.example.tech_interview_buddy.repository.util;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;


public class QueryDslPredicateBuilder {
    
    private final BooleanBuilder builder;
    
    public QueryDslPredicateBuilder() {
        this.builder = new BooleanBuilder();
    }
    
    public QueryDslPredicateBuilder(BooleanBuilder builder) {
        this.builder = builder;
    }
    
     public <T> QueryDslPredicateBuilder andIfNotNull(T value, Function<T, Predicate> predicateFunction) {
        if (value != null) {
            builder.and(predicateFunction.apply(value));
        }
        return this;
    }
    
     public QueryDslPredicateBuilder andIfNotBlank(String value, Function<String, Predicate> predicateFunction) {
        if (value != null && !value.trim().isEmpty()) {
            builder.and(predicateFunction.apply(value.trim()));
        }
        return this;
    }
    
    public <T> QueryDslPredicateBuilder andIfNotEmpty(Collection<T> collection, Function<Collection<T>, Predicate> predicateFunction) {
        if (collection != null && !collection.isEmpty()) {
            builder.and(predicateFunction.apply(collection));
        }
        return this;
    }
    
    public QueryDslPredicateBuilder andIfExists(boolean condition, Supplier<Predicate> existsPredicateSupplier) {
        if (condition) {
            builder.and(existsPredicateSupplier.get());
        }
        return this;
    }
    
    public QueryDslPredicateBuilder andIfNotExists(boolean condition, Supplier<Predicate> notExistsPredicateSupplier) {
        if (condition) {
            builder.and(notExistsPredicateSupplier.get());
        }
        return this;
    }
    
    public QueryDslPredicateBuilder andIfTrue(Boolean condition, Supplier<Predicate> predicateSupplier) {
        if (Boolean.TRUE.equals(condition)) {
            builder.and(predicateSupplier.get());
        }
        return this;
    }
    
    public QueryDslPredicateBuilder andIfFalse(Boolean condition, Supplier<Predicate> predicateSupplier) {
        if (Boolean.FALSE.equals(condition)) {
            builder.and(predicateSupplier.get());
        }
        return this;
    }
    
    public QueryDslPredicateBuilder and(Predicate predicate) {
        builder.and(predicate);
        return this;
    }
    
    public BooleanBuilder build() {
        return builder;
    }
    
    public static QueryDslPredicateBuilder newBuilder() {
        return new QueryDslPredicateBuilder();
    }
    
    public static QueryDslPredicateBuilder newBuilder(BooleanBuilder builder) {
        return new QueryDslPredicateBuilder(builder);
    }
}
