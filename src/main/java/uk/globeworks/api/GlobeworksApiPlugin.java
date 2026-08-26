package uk.globeworks.api;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Marker plugin that exposes the shared Globeworks API classes on the server classpath.
 * <p>
 * Contains no runtime logic of its own. Other plugins declare:
 * <pre>
 * depend: [GlobeworksAPI]
 * </pre>
 * (or softdepend) so they load after this plugin and share the same classloader copy
 * of {@link GenericHistoryEvent}, {@link NationRef}, etc.
 */
public final class GlobeworksApiPlugin extends JavaPlugin {


    String logo = """
 _______ _____   _______ ______ _______ 
|     __|     |_|       |   __ \\    ___|
|    |  |       |   -   |   __ <    ___|
|_______|_______|_______|______/_______|
 ________ _______ ______ __  __ _______ 
|  |  |  |       |   __ \\  |/  |     __|
|  |  |  |   -   |      <     <|__     |
|________|_______|___|__|__|\\__|_______|

                  <API>

""";

    @Override
    public void onEnable() {
        getLogger().info(logo);
        getLogger().info("GlobeworksAPI " + getPluginMeta().getVersion() + " loaded (event bus classes available).");        
    }
}
