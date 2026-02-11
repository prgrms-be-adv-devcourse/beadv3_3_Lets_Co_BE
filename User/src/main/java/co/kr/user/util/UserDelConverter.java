package co.kr.user.util;

import co.kr.user.model.vo.UserDel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserDelConverter implements AttributeConverter<UserDel, Integer> {
    @Override
    public Integer convertToDatabaseColumn(UserDel attribute) {
        return (attribute == null) ? null : attribute.getValue();
    }

    @Override
    public UserDel convertToEntityAttribute(Integer dbData) {
        return (dbData == null) ? null : UserDel.fromValue(dbData);
    }
}