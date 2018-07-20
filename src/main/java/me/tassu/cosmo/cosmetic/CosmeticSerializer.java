/*
 * The MIT License
 *
 * Copyright © 2018 Tassu <hello@tassu.me>
 * Copyright © 2018 Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.tassu.cosmo.cosmetic;

import com.google.common.reflect.TypeToken;
import lombok.val;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

@SuppressWarnings("UnstableApiUsage")
public class CosmeticSerializer implements TypeSerializer<AbstractCosmetic> {
    @Override
    public AbstractCosmetic deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        try {
            val cosmeticType = CosmeticType.valueOf(value.getNode("type").getString());

            switch (cosmeticType) {
                case HEAD:
                    return HeadCosmetic.builder()
                            .identifier(value.getNode("id").getString())
                            .name(value.getNode("name").getString())
                            .texture(value.getNode("texture").getString())
                            .build();
                default:
                    throw new IllegalArgumentException("Un-implemented cosmetic type " + cosmeticType.name());
            }
        } catch (Exception e) {
            throw new ObjectMappingException(e);
        }
    }

    @Override
    public void serialize(TypeToken<?> type, AbstractCosmetic obj, ConfigurationNode value) throws ObjectMappingException {
        try {
            val cosmeticType = obj.getType();

            value.getNode("type").setValue(obj.getType().name());
            value.getNode("id").setValue(obj.getIdentifier());
            value.getNode("name").setValue(obj.getName());

            switch (cosmeticType) {
                case HEAD:
                    val head = (HeadCosmetic) obj;
                    value.getNode("texture").setValue(head.getTexture());
                    break;
                default:
                    throw new IllegalArgumentException("Un-implemented cosmetic type " + cosmeticType.name());
            }
        } catch (Exception e) {
            throw new ObjectMappingException(e);
        }

    }
}
