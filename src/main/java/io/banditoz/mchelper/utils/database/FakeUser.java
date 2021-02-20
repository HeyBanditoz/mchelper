package io.banditoz.mchelper.utils.database;

import java.util.Objects;

public class FakeUser {
    private final long id;
    private final String username;

    public FakeUser(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeUser fakeUser = (FakeUser) o;
        return id == fakeUser.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
