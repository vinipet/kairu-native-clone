package com.kairu.core.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CommandCatalogTest {

    private CommandCatalog catalog;

    @BeforeEach
    void setup() {
        catalog = new CommandCatalog();
    }

    private Command<?> command(String name, String... aliases) {
        return new Command<>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Set<String> aliases() {
                return new HashSet<>(Arrays.asList(aliases));
            }

            @Override
            public String description() {
                return "test command";
            }

            @Override
            public CommandResult<Object> execute(String[] args) {
                return CommandResult.success(name);
            }
        };
    }

    @Test
    void shouldFindCommandWithoutAliases() {
        Command<?> cmd = command("start");

        catalog.register(cmd);

        assertTrue(catalog.find("start").isPresent());
        assertSame(cmd, catalog.find("start").get());
    }

    @Test
    void shouldFindCommandAndAliasesPointingToSameObject() {
        Command<?> cmd = command("start", "go", "begin");

        catalog.register(cmd);

        assertSame(catalog.find("start").get(), cmd);
        assertSame(catalog.find("go").get(), cmd);
        assertSame(catalog.find("begin").get(), cmd);
        assertSame(catalog.find("start").get(), catalog.find("go").get());
        assertSame(catalog.find("go").get(), catalog.find("begin").get());
    }

    @Test
    void shouldRegisterMultipleCommandsAndReturnExactlyNWithoutDuplicates() {
        Command<?> a = command("a", "a1", "a2");
        Command<?> b = command("b", "b1");
        Command<?> c = command("c", "c1", "c2", "c3");

        catalog.register(a);
        catalog.register(b);
        catalog.register(c);

        Set<Command<?>> all = catalog.findAll();
        assertEquals(3, all.size());
        assertTrue(all.contains(a));
        assertTrue(all.contains(b));
        assertTrue(all.contains(c));
    }

    @Test
    void shouldReturnEmptyWhenFindingUnknownName() {
        assertTrue(catalog.find("never-registered").isEmpty());
    }

    @Test
    void shouldThrowWhenRegisteringNullCommand() {
        assertThrows(NullPointerException.class, () -> catalog.register(null));
    }

    @Test
    void shouldThrowWhenCommandNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command(null)));
    }

    @Test
    void shouldThrowWhenCommandNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("  ")));
    }

    @Test
    void shouldThrowWhenRegisteringDuplicateName() {
        catalog.register(command("start", "go"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start")));
    }

    @Test
    void shouldThrowWhenAliasCollidesWithExistingCommandName() {
        catalog.register(command("stop"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "stop")));
    }

    @Test
    void shouldThrowWhenAliasCollidesWithExistingAlias() {
        catalog.register(command("stop", "halt"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("pause", "halt")));
    }

    @Test
    void shouldThrowWhenAliasIsNull() {
        Set<String> aliasesWithNull = new HashSet<>();
        aliasesWithNull.add(null);
        Command<?> cmd = new Command<>() {
            @Override
            public String name() {
                return "start";
            }

            @Override
            public Set<String> aliases() {
                return aliasesWithNull;
            }

            @Override
            public String description() {
                return "test command";
            }

            @Override
            public CommandResult<Object> execute(String[] args) {
                return CommandResult.success("start");
            }
        };

        assertThrows(IllegalArgumentException.class, () -> catalog.register(cmd));
    }

    @Test
    void shouldThrowWhenAliasIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", " ")));
    }

    @Test
    void shouldThrowWhenAliasEqualsOwnName() {
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "start")));
    }

    @Test
    void shouldNotChangeCatalogStateWhenRegisterFails() {
        Command<?> existing = command("stop");
        catalog.register(existing);

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "stop")));

        assertTrue(catalog.find("start").isEmpty());
        Set<Command<?>> all = catalog.findAll();
        assertEquals(1, all.size());
        assertTrue(all.contains(existing));
    }

    @Test
    void shouldValidateEverythingBeforeAnyPut() {
        catalog.register(command("clash"));

        Command<?> invalid = command("validName", "clash");

        assertThrows(IllegalArgumentException.class, () -> catalog.register(invalid));

        assertTrue(catalog.find("validName").isEmpty(),
                "o nome nao pode ficar registrado quando a validacao falha");
        assertEquals(1, catalog.findAll().size());
    }

    @Test
    void shouldNotExposeInternalStructureThroughFindAll() {
        catalog.register(command("start", "go"));
        catalog.register(command("stop"));

        Set<Command<?>> all = catalog.findAll();
        all.clear();

        assertEquals(2, catalog.findAll().size());
    }

    @Test
    void shouldNotThrowWhenFindingNullOrEmpty() {
        assertDoesNotThrow(() -> catalog.find(null));
        assertDoesNotThrow(() -> catalog.find(""));

        assertTrue(catalog.find(null).isEmpty());
        assertTrue(catalog.find("").isEmpty());
    }

    @Test
    void shouldReturnEmptySetWhenEmpty() {
        assertNotNull(catalog.findAll());
        assertTrue(catalog.findAll().isEmpty());
    }

    @Test
    void shouldAllowRegisteringCommandAfterFailedAttempt() {
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "start")));

        Command<?> cmd = command("start", "go");
        catalog.register(cmd);

        assertSame(cmd, catalog.find("start").get());
        assertEquals(1, catalog.findAll().size());
    }

    @Test
    void shouldThrowWhenNewCommandNameCollidesWithExistingAlias() {
        catalog.register(command("stop", "halt"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("halt", "other")));
    }

    @Test
    void shouldNotRegisterAnythingWhenAFailingAliasComesLast() {
        catalog.register(command("stop", "halt"));

        Command<?> invalid = command("start", "go", "halt");

        assertThrows(IllegalArgumentException.class, () -> catalog.register(invalid));

        assertTrue(catalog.find("start").isEmpty(), "nome nao pode ficar registrado");
        assertTrue(catalog.find("go").isEmpty(), "alias valido anterior nao pode ficar registrado");
        assertEquals(1, catalog.findAll().size());
        assertTrue(catalog.findAll().contains(catalog.find("stop").get()));
    }

    @Test
    void shouldFindNamesCaseInsensitively() {
        Command<?> cmd = command("start");

        catalog.register(cmd);

        assertSame(cmd, catalog.find("start").get());
        assertSame(cmd, catalog.find("Start").get());
        assertSame(cmd, catalog.find("START").get());
        assertSame(cmd, catalog.find("sTaRt").get());
    }

    @Test
    void shouldFindAliasesCaseInsensitively() {
        Command<?> cmd = command("start", "go");

        catalog.register(cmd);

        assertSame(cmd, catalog.find("go").get());
        assertSame(cmd, catalog.find("GO").get());
        assertSame(cmd, catalog.find("Go").get());
    }

    @Test
    void shouldRejectNameDuplicatedWithDifferentCase() {
        catalog.register(command("start", "go"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("START")));
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("Start", "other")));
    }

    @Test
    void shouldRejectAliasCollidingWithExistingNameIgnoringCase() {
        catalog.register(command("stop"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "STOP")));
    }

    @Test
    void shouldRejectAliasCollidingWithExistingAliasIgnoringCase() {
        catalog.register(command("stop", "halt"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("pause", "HALT")));
    }

    @Test
    void shouldRejectAliasEqualOwnNameIgnoringCase() {
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "START")));
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "sTaRt")));
    }

    @Test
    void shouldRejectDuplicateAliasesWithinSameCommandIgnoringCase() {
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "go", "GO")));
    }

    @Test
    void shouldNotRegisterAnythingWhenDuplicateAliasIsFound() {
        Command<?> invalid = command("start", "go", "GO");

        assertThrows(IllegalArgumentException.class, () -> catalog.register(invalid));

        assertTrue(catalog.find("start").isEmpty());
        assertTrue(catalog.find("go").isEmpty());
        assertEquals(0, catalog.findAll().size());
    }

    @Test
    void shouldTrimWhitespaceAroundName() {
        Command<?> cmd = command("  start  ");

        catalog.register(cmd);

        assertSame(cmd, catalog.find("start").get());
        assertSame(cmd, catalog.find("  start  ").get());
        assertSame(cmd, catalog.find("   START   ").get());
    }

    @Test
    void shouldCollapseDuplicateInternalSpacesInName() {
        Command<?> cmd = command("my   command");

        catalog.register(cmd);

        assertSame(cmd, catalog.find("my command").get());
        assertSame(cmd, catalog.find("  my   command  ").get());
    }

    @Test
    void shouldTreatNamesWithDifferentInternalSpacingAsCollision() {
        catalog.register(command("my command"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("my   command")));
        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("  my command  ")));
    }

    @Test
    void shouldTrimWhitespaceAroundAlias() {
        Command<?> cmd = command("start", "  go  ");

        catalog.register(cmd);

        assertSame(cmd, catalog.find("go").get());
        assertSame(cmd, catalog.find("  GO  ").get());
    }

    @Test
    void shouldCollapseDuplicateInternalSpacesInAlias() {
        Command<?> cmd = command("start", "go   fast");

        catalog.register(cmd);

        assertSame(cmd, catalog.find("go fast").get());
    }

    @Test
    void shouldRejectNameThatIsOnlyWhitespace() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> catalog.register(command("   \t ")));
        assertEquals("command must have a name", ex.getMessage());

        assertEquals(0, catalog.findAll().size());
    }

    @Test
    void shouldTreatWhitespaceVariantOfExistingNameAsCollisionAndChangeNothing() {
        catalog.register(command("start", "go"));

        assertThrows(IllegalArgumentException.class, () -> catalog.register(command("  START  ")));

        assertTrue(catalog.find("  START  ").isPresent());
        assertEquals(1, catalog.findAll().size());
    }

    @Test
    void shouldReportGrammarFixedErrorMessages() {
        NullPointerException nullCmd = assertThrows(NullPointerException.class, () -> catalog.register(null));
        assertEquals("command cannot be null", nullCmd.getMessage());

        IllegalArgumentException blankName =
                assertThrows(IllegalArgumentException.class, () -> catalog.register(command("  ")));
        assertEquals("command must have a name", blankName.getMessage());

        catalog.register(command("stop", "halt"));

        IllegalArgumentException dupName =
                assertThrows(IllegalArgumentException.class, () -> catalog.register(command("stop")));
        assertEquals("this command is already registered", dupName.getMessage());

        IllegalArgumentException aliasCollision =
                assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "stop")));
        assertEquals("the alias 'stop' is already used by another command", aliasCollision.getMessage());

        IllegalArgumentException ownName =
                assertThrows(IllegalArgumentException.class, () -> catalog.register(command("start", "START")));
        assertEquals("this command has an alias with the same name as the command itself", ownName.getMessage());

        IllegalArgumentException emptyAlias = assertThrows(IllegalArgumentException.class,
                () -> catalog.register(new Command<>() {
                    @Override
                    public String name() {
                        return "start";
                    }

                    @Override
                    public Set<String> aliases() {
                        Set<String> aliases = new HashSet<>();
                        aliases.add(" ");
                        return aliases;
                    }

                    @Override
                    public String description() {
                        return "test command";
                    }

                    @Override
                    public CommandResult<Object> execute(String[] args) {
                        return CommandResult.success("start");
                    }
                }));
        assertEquals("this command has an empty alias", emptyAlias.getMessage());
    }

    @Test
    void shouldNotThrowWhenFindingNameWithOnlySpaces() {
        assertDoesNotThrow(() -> catalog.find("   "));

        assertTrue(catalog.find("   ").isEmpty());
    }

    @Test
    void shouldFindAllEvenWhenCommandsShareTheSameDescription() {
        Command<?> start = command("start", "go");
        Command<?> stop = command("stop");

        catalog.register(start);
        catalog.register(stop);

        assertEquals(2, catalog.findAll().size());
        assertTrue(catalog.findAll().contains(start));
        assertTrue(catalog.findAll().contains(stop));
    }
}
