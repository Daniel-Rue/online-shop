package ru.kon.onlineshop.service.impl;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.kon.onlineshop.entity.*;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProductSpecificationImpl {

    public static Specification<Product> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> hasMinRating(Double minRating) {
        if (minRating == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Subquery<Double> ratingSubquery = query.subquery(Double.class);
            Root<Review> reviewRoot = ratingSubquery.from(Review.class);
            ratingSubquery.select(criteriaBuilder.avg(reviewRoot.get("rating")))
                    .where(criteriaBuilder.equal(reviewRoot.get("product"), root));

            Expression<Double> avgRating = ratingSubquery.getSelection();
            Expression<Double> ratingOrZero = criteriaBuilder.coalesce(avgRating, 0.0);


            return criteriaBuilder.greaterThanOrEqualTo(ratingOrZero, minRating);
        };
    }

    public static Specification<Product> hasAttributeValue(Long attributeId, String value, AttributeType attributeType) {
        if (attributeId == null || !StringUtils.hasText(value)) {
            return null; // Не фильтруем, если нет ID или значения
        }

        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            Join<Product, ProductAttributeValue> pavJoin = root.join("attributeValues", JoinType.INNER);
            Join<ProductAttributeValue, Attribute> attrJoin = pavJoin.join("attribute", JoinType.INNER);

            Predicate attributeIdPredicate = criteriaBuilder.equal(attrJoin.get("id"), attributeId);

            Predicate valuePredicate;
            try {
                switch (attributeType) {
                    case STRING:
                        valuePredicate = criteriaBuilder.equal(
                                criteriaBuilder.lower(pavJoin.get("value")),
                                value.toLowerCase()
                        );
                        break;
                    case NUMBER:
                        if (value.contains(":")) {
                            String[] parts = value.split(":", -1);
                            BigDecimal min = StringUtils.hasText(parts[0]) ? new BigDecimal(parts[0]) : null;
                            BigDecimal max = StringUtils.hasText(parts[1]) ? new BigDecimal(parts[1]) : null;
                            List<Predicate> rangePredicates = new ArrayList<>();
                            Expression<BigDecimal> numericValueExpr = pavJoin.get("value").as(BigDecimal.class);
                            if (min != null) {
                                rangePredicates.add(criteriaBuilder.greaterThanOrEqualTo(numericValueExpr, min));
                            }
                            if (max != null) {
                                rangePredicates.add(criteriaBuilder.lessThanOrEqualTo(numericValueExpr, max));
                            }
                            valuePredicate = criteriaBuilder.and(rangePredicates.toArray(new Predicate[0]));

                        } else {
                            BigDecimal numericValue = new BigDecimal(value);
                            valuePredicate = criteriaBuilder.equal(pavJoin.get("value").as(BigDecimal.class), numericValue);
                        }
                        break;
                    case BOOLEAN:
                        Boolean booleanValue = Boolean.parseBoolean(value);
                        valuePredicate = criteriaBuilder.equal(pavJoin.get("value").as(Boolean.class), booleanValue);
                        break;
                    default:
                        throw new IllegalArgumentException("Неподдерживаемый тип атрибута для фильтрации: " + attributeType);
                }
            } catch (NumberFormatException e) {
                System.err.println("Ошибка парсинга числового значения '" + value + "' для атрибута ID " + attributeId);
                return criteriaBuilder.disjunction();
            } catch (IllegalArgumentException e) {
                System.err.println("Ошибка парсинга значения '" + value + "' для атрибута ID " + attributeId + ": " + e.getMessage());
                return criteriaBuilder.disjunction();
            }


            return criteriaBuilder.and(attributeIdPredicate, valuePredicate);
        };
    }

    public static Specification<Product> inCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<Product, Category> categoryJoin = root.join("categories", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Product> inCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<Product, Category> categoryJoin = root.join("categories", JoinType.INNER);
            return categoryJoin.get("id").in(categoryIds);
        };
    }

}
