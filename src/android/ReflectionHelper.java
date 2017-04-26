package net.kuama.backgroundservice;

class ReflectionHelper {

    static final Class<?> findClassByName(String classname, String searchPackages) {

        try {
            return Class.forName(searchPackages + "." + classname);
        } catch (ClassNotFoundException e) {
            //not in this package, try another
        }

        //nothing found: return null or throw ClassNotFoundException
        return null;
    }

}
