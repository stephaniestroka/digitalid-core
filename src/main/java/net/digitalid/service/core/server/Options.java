package net.digitalid.service.core.server;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.service.core.storage.Service;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.system.console.Console;
import net.digitalid.utility.system.console.Option;
import net.digitalid.utility.system.console.exceptions.EscapeOptionException;
import net.digitalid.utility.exceptions.external.ExternalException;

/**
 * This class contains the command-line options of the {@link Server}.
 * 
 * @see Option
 * @see Console
 */
final class Options {
    
    /**
     * Starts the console with the command-line options.
     */
    static void start() {
        Console.write();
        Console.addOption(new ExitServer());
        Console.addOption(new ShowVersion());
        Console.addOption(new ShowHosts());
        Console.addOption(new CreateHost());
        Console.addOption(new ExportHost());
        Console.addOption(new ImportHost());
        Console.addOption(new ShowServices());
        Console.addOption(new LoadServices());
        Console.addOption(new ActivateService());
        Console.addOption(new DeactivateService());
        Console.addOption(new ChangeProvider());
        Console.addOption(new GenerateTokens());
        Console.addOption(new ShowMembers());
        Console.addOption(new AddMembers());
        Console.addOption(new RemoveMembers());
        Console.addOption(new OpenHost());
        Console.addOption(new CloseHost());
        Console.start();
    }
    
    /**
     * Prompts the user to select one of the hosts that run on the server.
     * 
     * @return the selected host or a {@link EscapeOptionException} if the user escaped.
     */
    private static @Nonnull Host selectHost() throws EscapeOptionException {
        final @Nonnull ReadOnlyList<Host> hosts = FreezableArrayList.getNonNullable((Collection<? extends Host>) Server.getHosts()).freeze();
        if (!hosts.isEmpty()) {
            Console.write("Please select one of the following hosts:");
            Console.write("- 0: [Escape]");
            int i = 1;
            for (final @Nonnull Host host : hosts) {
                Console.write("- " + (i++) + ": " + host.getIdentifier());
            }
            Console.write();
            final int input = Console.readNumber("Choice: ", 0) - 1;
            Console.write();
            if (input >= 0 && input < hosts.size()) {
                return hosts.getNonNullable(input);
            } else if (input > 0) {
                Console.write("Please choose one of the given options!");
            }
        } else {
            Console.write("There are no hosts to proceed with.");
        }
        throw new EscapeOptionException();
    }
    
    
    /**
     * This option exits the server.
     */
    private static final class ExitServer extends Option {
        
        ExitServer() { super("Exit the server."); }
        
        @Override
        public void execute() {
            // TODO: Remove the true on the following line!
            if (true || Console.readBoolean("Are you sure you want to shut down the server? Yes or no (the default is no): ", false)) {
                Console.write("The server is shutting down...");
                Console.write();
                Server.shutDown();
            }
        }
        
    }
    
    /**
     * This option shows the version and the authors.
     */
    private static final class ShowVersion extends Option {
        
        ShowVersion() { super("Show the version."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("Version: " + Server.VERSION + " (" + Server.DATE + ")");
            Console.write("Authors: " + Server.AUTHORS);
        }
        
    }
    
    /**
     * This option shows the hosts.
     */
    private static final class ShowHosts extends Option {
        
        ShowHosts() { super("Show the hosts."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("The following hosts are running on this server:");
            for (final @Nonnull Host host : Server.getHosts()) {
                Console.write("- " + host.getIdentifier().getString());
            }
            if (Server.getHosts().isEmpty()) { Console.write("(None)"); }
        }
        
    }
    
    /**
     * This option adds a new host.
     */
    private static final class CreateHost extends Option {
        
        CreateHost() { super("Create a host."); }
        
        @Override
        @Committing
        public void execute() {
            if (Console.readBoolean("Are you sure you want to add a new host? Yes or no (the default is no): ", false)) {
                @Nonnull String string = Console.readString("Please enter the identifier of the new host: ", null);
                while (!HostIdentifier.isValid(string)) {
                    string = Console.readString("Bad input! Please enter a valid host identifier: ", null);
                }
                final @Nonnull HostIdentifier identifier = new HostIdentifier(string);
                try {
                    new Host(identifier);
                } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
                    Console.write("Could not create the host " + identifier + " (" + exception + ").");
                    Database.rollback();
                }
            }
        }
        
    }
    
    /**
     * This option exports a host.
     */
    private static final class ExportHost extends Option {
        
        ExportHost() { super("Export a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option imports a host.
     */
    private static final class ImportHost extends Option {
        
        ImportHost() { super("Import a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option shows the installed services.
     */
    private static final class ShowServices extends Option {
        
        ShowServices() { super("Show the services."); }
        
        @Override
        @Committing
        public void execute() {
            final @Nonnull ReadOnlyCollection<Service> services = Service.getServices();
            Console.write("The following services are installed on this server:");
            for (final @Nonnull Service service : services) {
                Console.write("- " + service.getNameWithVersion());
            }
            if (services.isEmpty()) { Console.write("(None)"); }
        }
        
    }
    
    /**
     * This option reloads the available services.
     */
    private static final class LoadServices extends Option {
        
        LoadServices() { super("Reload the services."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("The services are reloaded.");
            Server.loadServices();
        }
        
    }
    
    /**
     * This option activates a service.
     */
    private static final class ActivateService extends Option {
        
        ActivateService() { super("Activate a service."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option deactivates a service.
     */
    private static final class DeactivateService extends Option {
        
        DeactivateService() { super("Deactivate a service."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option changes a provider.
     */
    private static final class ChangeProvider extends Option {
        
        ChangeProvider() { super("Change a provider."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option generates tokens.
     */
    private static final class GenerateTokens extends Option {
        
        GenerateTokens() { super("Generate tokens."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option shows members.
     */
    private static final class ShowMembers extends Option {
        
        ShowMembers() { super("Show members."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option adds members.
     */
    private static final class AddMembers extends Option {
        
        AddMembers() { super("Add members."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option removes members.
     */
    private static final class RemoveMembers extends Option {
        
        RemoveMembers() { super("Remove members."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option opens a host.
     */
    private static final class OpenHost extends Option {
        
        OpenHost() { super("Open a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
    /**
     * This option closes a host.
     */
    private static final class CloseHost extends Option {
        
        CloseHost() { super("Close a host."); }
        
        @Override
        @Committing
        public void execute() {
            Console.write("To be implemented."); // TODO
        }
        
    }
    
}
