/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.TupleType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code {@link BTupleType }} represents the tuple type.
 *
 * @since 0.966.0
 */
public class BTupleType extends BType implements TupleType {

    public List<BType> tupleTypes;
    public BType restType;
    public Boolean isAnyData = null;
    public boolean resolvingToString = false;

    public BIntersectionType immutableType;

    public BTupleType(List<BType> tupleTypes) {
        super(TypeTags.TUPLE, null);
        this.tupleTypes = tupleTypes;
    }

    public BTupleType(BTypeSymbol tsymbol, List<BType> tupleTypes) {
        super(TypeTags.TUPLE, tsymbol);
        this.tupleTypes = tupleTypes;
    }

    public BTupleType(BTypeSymbol tsymbol, List<BType> tupleTypes, BType restType, long flags) {
        super(TypeTags.TUPLE, tsymbol, flags);
        this.tupleTypes = tupleTypes;
        this.restType = restType;
    }

    public BTupleType(BTypeSymbol tsymbol, List<BType> tupleTypes, BType restType, long flags, boolean isCyclic) {
        super(TypeTags.TUPLE, tsymbol, flags);
        this.tupleTypes = tupleTypes;
        this.restType = restType;
        this.isCyclic = isCyclic;
    }

    @Override
    public List<BType> getTupleTypes() {
        return tupleTypes;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TUPLE;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // This logic is added to prevent duplicate recursive calls to toString
        if (this.resolvingToString) {
            if (tsymbol != null && !tsymbol.getName().getValue().isEmpty()) {
                return this.tsymbol.toString();
            }
            return "...";
        }
        this.resolvingToString = true;

        String stringRep = "[" + tupleTypes.stream().map(BType::toString).collect(Collectors.joining(","))
                + ((restType != null) ? (tupleTypes.size() > 0 ? "," : "") + restType.toString() + "...]" : "]");

        this.resolvingToString = false;
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? stringRep : stringRep.concat(" & readonly");
    }

    @Override
    public BIntersectionType getImmutableType() {
        return this.immutableType;
    }

    // In the case of a reference based cyclic tuple, this aids in
    //adding resolved members to a previously defined empty tuple shell in main scope
    public boolean addMembers(BType memberType) {
        // Prevent cyclic types of same type ex: type Foo [int, Foo];
        if (memberType.isCyclic && memberType.getQualifiedTypeName().equals(this.getQualifiedTypeName())) {
            return false;
        }
        this.tupleTypes.add(memberType);
        return true;
    }
}
