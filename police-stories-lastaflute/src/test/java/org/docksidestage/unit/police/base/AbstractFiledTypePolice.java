/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.unit.police.base;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;

/**
 * フィールドの名前と型の組み合わせをチェックしたいポリス用。
 * @author mito
 * @author jflute
 */
public abstract class AbstractFiledTypePolice extends AbstractJavaPolice<AbstractFiledTypePolice> {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final List<String> defaultTargetClassSuffixList = Arrays.asList("Form", "ContentResult", "Part");
    protected List<String> targetClassSuffixList;
    protected List<String> ignoreFiledNameList = DfCollectionUtil.newArrayList();

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    public AbstractFiledTypePolice setIgnoreFieldName(String... fieldName) {
        ignoreFiledNameList = Arrays.asList(fieldName);

        return this;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    public Optional<String> validate(File srcFile, Class<?> clazz) {
        List<String> errorMessageList = validateField(clazz);
        if (!errorMessageList.isEmpty()) {
            return Optional.of(String.join("\n", errorMessageList));
        }

        // インナークラス対応
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            handle(srcFile, innerClass);
        }

        return Optional.empty();
    }

    // return error message list.
    protected List<String> validateField(Class<?> clazz) {
        List<Field> fieldList = DfReflectionUtil.getPublicFieldList(clazz);

        return fieldList.stream().map(field -> {
            return (isTargetField(field) && !ignoreFiledNameList.contains(field.getName()) && !validateFieldType(field))
                    ? String.format("%s.%s", clazz.getName(), field.getName()) : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    protected abstract boolean isTargetField(Field field);

    protected abstract boolean validateFieldType(Field field);

    public AbstractFiledTypePolice setTargetClassSuffix(String... suffixArray) {
        targetClassSuffixList = Arrays.asList(suffixArray);
        return this;
    }
}
