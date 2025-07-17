package ru.red.lazaruscloud.mapper;

import ru.red.lazaruscloud.model.LazarusUserDetail;
import ru.red.lazaruscloud.model.User;

public class UserToLazarusUserDetaMapper {

    public static LazarusUserDetail toUserDetail(User user) {
        return new LazarusUserDetail(user);
    }
}
