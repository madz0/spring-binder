package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.binding.MethodUtils;
import com.github.madz0.springbinder.binding.property.*;

import javax.persistence.metamodel.Attribute;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Groups {

    public interface IGroup {
        default Set<IProperty> getProperties() {
            return props();
        }

        default Set<IProperty> props(IProperty... props) {
            return new HashSet<>(Arrays.asList(props));
        }

        @SuppressWarnings("unchecked")
        default Set<IProperty> allProps(Class<? extends Model> clazz) {
            return Arrays.stream(BindingUtils.getBeanWrapper(clazz).getPropertyDescriptors())
                    .filter(x ->
                            !Objects.equals(x.getName(), "class") &&
                                    !Objects.equals(x.getName(), "recordType")
                    )
                    .map(x -> {
                        if (Collection.class.isAssignableFrom(x.getPropertyType())) {
                            Class<?> genericClass = (Class) ((ParameterizedType) x.getReadMethod().getGenericReturnType()).getActualTypeArguments()[0];
                            if (Model.class.isAssignableFrom(genericClass)) {
                                Class<? extends Model> baseModelGeneric = (Class<? extends Model>) genericClass;
                                return ComputedModelProperty.of(clazz, x.getReadMethod().getName(),
                                        props(field(IdModelFields.ID), computed(baseModelGeneric, Model::getPresentation)));
                            }
                        }
                        if (Model.class.isAssignableFrom(x.getPropertyType())) {
                            Class<? extends Model> genericClass = (Class<? extends Model>) x.getPropertyType();
                            return ComputedModelProperty.of(clazz, x.getReadMethod().getName(),
                                    props(field(IdModelFields.ID), computed(genericClass, Model::getPresentation)));
                        }
                        return ComputedProperty.of(clazz, x.getReadMethod().getName());
                    })
                    .collect(Collectors.toSet());
        }

        default Set<IProperty> allPropsExcept(Class<? extends Model> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, props(ignore));
        }

        default Set<IProperty> allPropsExcept(Class<? extends Model> clazz, Set<IProperty> ignore) {
            return allProps(clazz).stream()
                    .filter(x -> ignore.stream().noneMatch(y -> Objects.equals(x, y)))
                    .collect(Collectors.toSet());
        }

        default Set<IProperty> addProps(Class<? extends IGroup> clazz, IProperty... props) {
            Set<IProperty> set = new HashSet<>(BindingUtils.getPropertiesFromGroup(clazz));
            set.addAll(Arrays.asList(props));
            return set;
        }

        default Set<IProperty> removeProps(Class<? extends IGroup> clazz, IProperty... props) {
            Set<IProperty> set = new HashSet<>(BindingUtils.getPropertiesFromGroup(clazz));
            set.removeAll(Arrays.asList(props));
            return set;
        }

        default IProperty field(Attribute attribute) {
            return FieldProperty.of(attribute);
        }

        default IProperty field(String name) {
            return FieldProperty.of(name);
        }

        default IProperty model(Attribute attribute, IProperty... fields) {
            return ModelProperty.of(attribute, props(fields));
        }

        default IProperty model(String name, IProperty... fields) {
            return ModelProperty.of(name, props(fields));
        }

        default <T> IProperty computed(Class<T> clazz, Function<T, ?> mapFunc) {
            return ComputedProperty.of(clazz, MethodUtils.getMethodName(clazz, mapFunc));
        }

        default <T> IProperty computedModel(Class<T> clazz, Function<T, ?> mapFunc, IProperty... fields) {
            return ComputedModelProperty.of(clazz, MethodUtils.getMethodName(clazz, mapFunc), props(fields));
        }
    }

    public interface IList extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModelFields.ID));
        }

        default Set<IProperty> listProps(Class<? extends Model> clazz, IProperty... props) {
            return Stream.concat(Stream.of(field(IdModelFields.ID), computed(clazz, Model::getPresentation),
                    computed(clazz, AccessModel::getAccess)), Arrays.stream(props)).collect(Collectors.toSet());
        }

        default Set<IProperty> allListProps(Class<? extends Model> clazz) {
            return allListPropsExcept(clazz);
        }

        default Set<IProperty> allListPropsExcept(Class<? extends Model> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, Stream.concat(Stream.of(field(ModelFields.VERSION), field(ModelFields.CREATED_BY),
                    field(ModelFields.MODIFIED_BY), field(ModelFields.CREATED_DATE), field(ModelFields.MODIFIED_DATE)),
                    Arrays.stream(ignore)).collect(Collectors.toSet()));
        }
    }

    public interface ICreate extends IGroup {
        default Set<IProperty> createProps(Class<? extends Model> clazz, IProperty... props) {
            return props(props);
        }

        default Set<IProperty> allCreateProps(Class<? extends Model> clazz) {
            return allCreatePropsExcept(clazz);
        }

        default Set<IProperty> allCreatePropsExcept(Class<? extends Model> clazz, IProperty... ignore) {
            return allPropsExcept(clazz,
                    Stream.concat(
                            Stream.of(
                                    field(IdModelFields.ID),
                                    field(ModelFields.VERSION),
                                    computed(clazz, AccessModel::getAccess),
                                    field(ModelFields.CREATED_BY),
                                    computed(clazz, Model::getPresentation),
                                    field(ModelFields.MODIFIED_BY),
                                    field(ModelFields.CREATED_DATE),
                                    field(ModelFields.MODIFIED_DATE)),
                            Arrays.stream(ignore)).collect(Collectors.toSet()
                    )
            );
        }
    }

    public interface IEdit extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModelFields.ID), field(ModelFields.VERSION));
        }

        default Set<IProperty> editProps(Class<? extends Model> clazz, IProperty... props) {
            return Stream.concat(Stream.of(
                    field(IdModelFields.ID),
                    field(ModelFields.VERSION),
                    computed(clazz, Model::getPresentation),
                    computed(clazz, AccessModel::getAccess)
            ),
                    Arrays.stream(props)).collect(Collectors.toSet());
        }

        default Set<IProperty> allEditProps(Class<? extends Model> clazz) {
            return allEditPropsExcept(clazz);
        }

        default Set<IProperty> allEditPropsExcept(Class<? extends Model> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, Stream.concat(
                    Stream.of(
                            field(ModelFields.CREATED_BY),
                            field(ModelFields.MODIFIED_BY),
                            field(ModelFields.CREATED_DATE),
                            field(ModelFields.MODIFIED_DATE)),
                    Arrays.stream(ignore)).collect(Collectors.toSet()));
        }
    }

    public interface IView extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModelFields.ID), field(ModelFields.CREATED_DATE),
                    field(ModelFields.MODIFIED_DATE), field(AccessModelFields.ACCESS),
                    model(ModelFields.CREATED_BY, field(ModelFields.PRESENTATION)),
                    model(ModelFields.MODIFIED_BY, field(ModelFields.PRESENTATION)));
        }

        default Set<IProperty> viewProps(Class<? extends Model> clazz, IProperty... props) {
            return Stream.concat(
                    Stream.of(
                            field(IdModelFields.ID),
                            field(ModelFields.CREATED_DATE),
                            field(ModelFields.MODIFIED_DATE),
                            computed(clazz, Model::getPresentation),
                            computed(clazz, AccessModel::getAccess),
                            model(ModelFields.CREATED_BY, field(ModelFields.PRESENTATION)),
                            model(ModelFields.MODIFIED_BY, field(ModelFields.PRESENTATION))
                    ),
                    Arrays.stream(props)).collect(Collectors.toSet()
            );
        }

        default Set<IProperty> allViewProps(Class<? extends Model> clazz) {
            return allViewPropsExcept(clazz);
        }

        default Set<IProperty> allViewPropsExcept(Class<? extends Model> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, props(ignore));
        }
    }

    public interface IDelete extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModelFields.ID));
        }
    }

    public interface IDto extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModelFields.ID), field(ModelFields.RECORD_TYPE));
        }
    }
}
