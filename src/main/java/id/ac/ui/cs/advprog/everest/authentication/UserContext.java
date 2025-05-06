package id.ac.ui.cs.advprog.everest.authentication;

public class UserContext {

    private static final ThreadLocal<AuthenticatedUser> currentUser = new ThreadLocal<>();

    public static void setUser(AuthenticatedUser user) {
        currentUser.set(user);
    }

    public static AuthenticatedUser getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}
