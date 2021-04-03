package me.libraryaddict.core.fancymessage;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// TODO replace with BungeeCord's Chat API
/**
 * Represents a formattable message. Such messages can use elements such as colors, formatting codes, hover and click data, and
 * other features provided by the vanilla Minecraft <a href="http://minecraft.gamepedia.com/Tellraw#Raw_JSON_Text">JSON message
 * formatter</a>. This class allows plugins to emulate the functionality of the vanilla Minecraft
 * <a href="http://minecraft.gamepedia.com/Commands#tellraw">tellraw command</a>.
 * <p>
 * This class follows the builder pattern, allowing for method chaining. It is set up such that invocations of property-setting
 * methods will affect the current editing component, and a call to {@link #then()} or {@link #then(Object)} will append a new
 * editing component to the end of the message, optionally initializing it with text. Further property-setting method calls will
 * affect that editing component.
 * </p>
 */
public class FancyMessage implements Cloneable, ConfigurationSerializable {

    private static JsonParser _stringParser = new JsonParser();

    static {
        ConfigurationSerialization.registerClass(FancyMessage.class);
    }

    private ComponentBuilder builder;

    /**
     * Creates a JSON message without text.
     */
    public FancyMessage() {
        this((String) null);
    }

    /**
     * Creates a JSON message with text.
     *
     * @param firstPartText The existing text in the message.
     */
    public FancyMessage(final String firstPartText) {
        if (firstPartText == null)
            builder = new ComponentBuilder();
        else
            builder = new ComponentBuilder().append(TextComponent.fromLegacyText(firstPartText));
    }

    /**
     * Deserializes a JSON-represented message from a mapping of key-value pairs. This is called by the Bukkit serialization API.
     * It is not intended for direct public API consumption.
     *
     * @param serialized The key-value mapping which represents a fancy message.
     */
    @SuppressWarnings("unchecked")
    public static FancyMessage deserialize(Map<String, Object> serialized) {
        FancyMessage msg = new FancyMessage();
        deserialize(serialized.containsKey("JSON") ? serialized.get("JSON").toString() : null);
        return msg;
    }

    /**
     * Deserializes a fancy message from its JSON representation. This JSON representation is of the format of that returned by
     * {@link #toJSONString()}, and is compatible with vanilla inputs.
     *
     * @param json The JSON string which represents a fancy message.
     * @return A {@code FancyMessage} representing the parameterized JSON message.
     */
    public static FancyMessage deserialize(String json) {
        FancyMessage message = new FancyMessage();
        message.builder.append(ComponentSerializer.parse(json));
        return message;
    }

    @Override
    public FancyMessage clone() throws CloneNotSupportedException {
        FancyMessage instance = (FancyMessage) super.clone();
        instance.builder = new ComponentBuilder(builder);
        return instance;
    }

    /**
     * Sets the color of the current editing component to a value.
     *
     * @param color The new color of the current editing component.
     * @return This builder instance.
     * @throws IllegalArgumentException If the specified {@code ChatColor} enumeration value is not a color (but a format value).
     */
    public FancyMessage color(final ChatColor color) {
        builder.color(color.asBungee());
        return this;
    }

    /**
     * Set the behavior of the current editing component to instruct the client to send the specified string to the server as a
     * chat message when the currently edited part of the {@code FancyMessage} is clicked. The client <b>will</b> immediately send
     * the command to the server to be executed when the editing component is clicked.
     *
     * @param command The text to display in the chat bar of the client.
     * @return This builder instance.
     */
    public FancyMessage command(final String command) {
//        onClick("run_command", command);
        builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    /**
     * Set the behavior of the current editing component to instruct the client to open a file on the client side filesystem when
     * the currently edited part of the {@code FancyMessage} is clicked.
     *
     * @param path The path of the file on the client filesystem.
     * @return This builder instance.
     */
    public FancyMessage file(final String path) {
        throw new UnsupportedOperationException();
//        onClick("open_file", path);
//        return this;
    }

    /**
     * Set the behavior of the current editing component to display the specified lines of formatted text when the client hovers
     * over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param lines The lines of text which will be displayed to the client upon hovering. The iteration order of this object will
     *              be the order in which the lines of the tooltip are created.
     * @return This builder instance.
     */
    public FancyMessage formattedTooltip(final Collection<FancyMessage> lines) {
        return formattedTooltip(lines.toArray(new FancyMessage[0]));
    }

    /**
     * Set the behavior of the current editing component to display formatted text when the client hovers over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param text The formatted text which will be displayed to the client upon hovering.
     * @return This builder instance.
     */
    public FancyMessage formattedTooltip(FancyMessage text) {
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(text.builder.create())));
        return this;
    }

    /**
     * Set the behavior of the current editing component to display the specified lines of formatted text when the client hovers
     * over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param lines The lines of formatted text which will be displayed to the client upon hovering.
     * @return This builder instance.
     */
    public FancyMessage formattedTooltip(FancyMessage... lines) {
        // combine
        ComponentBuilder concat = new ComponentBuilder();
        for (int i = 0; i < lines.length; i++) {
            concat.append(lines[i].builder.create());
            if (i + 1 < lines.length) // if not last element
                concat.append("\n");
        }
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(concat.create())));
        return this; // Throws NPE if size is 0, intended
    }

    /**
     * Set the behavior of the current editing component to instruct the client to append the chat input box content with the
     * specified string when the currently edited part of the {@code FancyMessage} is SHIFT-CLICKED. The client will not
     * immediately send the command to the server to be executed unless the client player submits the command/chat message,
     * usually with the enter key.
     *
     * @param command The text to append to the chat bar of the client.
     * @return This builder instance.
     */
    public FancyMessage insert(final String command) {
        builder.insertion(command);
        return this;
    }

    /**
     * Set the behavior of the current editing component to display information about an item when the client hovers over the
     * text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param itemStack The stack for which to display information.
     * @return This builder instance.
     */
    public FancyMessage itemTooltip(final ItemStack itemStack) {
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, Bukkit.getItemFactory().hoverContentOf(itemStack)));
        return this;
    }

    /**
     * Set the behavior of the current editing component to display information about an item when the client hovers over the
     * text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param itemJSON A string representing the JSON-serialized NBT data tag of an {@link ItemStack}.
     * @return This builder instance.
     */
    public FancyMessage itemTooltip(final String itemJSON) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the behavior of the current editing component to instruct the client to open a webpage in the client's web browser when
     * the currently edited part of the {@code FancyMessage} is clicked.
     *
     * @param url The URL of the page to open when the link is clicked.
     * @return This builder instance.
     */
    public FancyMessage link(final String url) {
        builder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    /**
     * Sends this message to a command sender. If the sender is a player, they will receive the fully-fledged formatted display of
     * this message. Otherwise, they will receive a version of this message with less formatting.
     *
     * @param sender The command sender who will receive the message.
     * @see #toOldMessageFormat()
     */
    public void send(CommandSender sender) {
        sender.spigot().sendMessage(builder.create());
    }


    /**
     * Sends this message to multiple command senders.
     *
     * @param senders The command senders who will receive the message.
     * @see #send(CommandSender)
     */
    public void send(final Iterable<? extends CommandSender> senders) {
        BaseComponent[] components = builder.create();
        for (final CommandSender sender : senders) {
            sender.spigot().sendMessage(components);
        }
    }

    /**
     * Sends this message to a player. The player will receive the fully-fledged formatted display of this message.
     *
     * @param player The player who will receive the message.
     */
    public void send(Player player) {
        player.spigot().sendMessage(builder.create());
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        // map.put("messageParts", messageParts);
        map.put("JSON", ComponentSerializer.toString(builder.create()));
        return map;
    }

    /**
     * Set the behavior of the current editing component to display information about a parameterless statistic when the client
     * hovers over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param which The statistic to display.
     * @return This builder instance.
     * @throws IllegalArgumentException If the statistic requires a parameter which was not supplied.
     */
    public FancyMessage statisticTooltip(final Statistic which) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the behavior of the current editing component to display information about a statistic parameter with an entity type
     * when the client hovers over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param which  The statistic to display.
     * @param entity The sole entity type parameter to the statistic.
     * @return This builder instance.
     * @throws IllegalArgumentException If the statistic requires a parameter which was not supplied, or was supplied a parameter that was not
     *                                  required.
     */
    public FancyMessage statisticTooltip(final Statistic which, EntityType entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the behavior of the current editing component to display information about a statistic parameter with a material when
     * the client hovers over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param which The statistic to display.
     * @param item  The sole material parameter to the statistic.
     * @return This builder instance.
     * @throws IllegalArgumentException If the statistic requires a parameter which was not supplied, or was supplied a parameter that was not
     *                                  required.
     */
    public FancyMessage statisticTooltip(final Statistic which, Material item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the stylization of the current editing component.
     *
     * @param styles The array of styles to apply to the editing component.
     * @return This builder instance.
     * @throws IllegalArgumentException If any of the enumeration values in the array do not represent formatters.
     */
    public FancyMessage style(ChatColor... styles) {
        for (final ChatColor style : styles) {
            switch (style) {
                case BOLD:
                    builder.bold(true);
                    break;
                case ITALIC:
                    builder.italic(true);
                    break;
                case STRIKETHROUGH:
                    builder.strikethrough(true);
                    break;
                case MAGIC:
                    builder.obfuscated(true);
                    break;
                case RESET:
                    builder.reset();
                    break;
            }
        }
        return this;
    }

    /**
     * Set the behavior of the current editing component to instruct the client to replace the chat input box content with the
     * specified string when the currently edited part of the {@code FancyMessage} is clicked. The client will not immediately
     * send the command to the server to be executed unless the client player submits the command/chat message, usually with the
     * enter key.
     *
     * @param command The text to display in the chat bar of the client.
     * @return This builder instance.
     */
    public FancyMessage suggest(final String command) {
        builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return this;
    }

    /**
     * Sets the text of the current editing component to a value.
     *
     * @param text The new text of the current editing component.
     * @return This builder instance.
     */
    public FancyMessage text(String text) {
        BaseComponent component = builder.getCurrentComponent();
        if (component instanceof TextComponent) {
            ((TextComponent) component).setText(text);
        }
        return this;
    }

    /**
     * Terminate construction of the current editing component, and begin construction of a new message component. After a
     * successful call to this method, all setter methods will refer to a new message component, created as a result of the call
     * to this method.
     *
     * @return This builder instance.
     */
    public FancyMessage then() {
        builder.append("");
        return this;
    }

    /**
     * Terminate construction of the current editing component, and begin construction of a new message component. After a
     * successful call to this method, all setter methods will refer to a new message component, created as a result of the call
     * to this method.
     *
     * @param text The text which will populate the new message component.
     * @return This builder instance.
     */
    public FancyMessage then(final String text) {
        builder.append(TextComponent.fromLegacyText(text));
        return this;
    }

    /**
     * Serialize this fancy message, converting it into syntactically-valid JSON using a {@link JsonWriter}. This JSON should be
     * compatible with vanilla formatter commands such as {@code /tellraw}.
     *
     * @return The JSON string representing this object.
     */
    public String toJSONString() {
        return ComponentSerializer.toString(builder.create());
    }

    /**
     * Convert this message to a human-readable string with limited formatting. This method is used to send this message to
     * clients without JSON formatting support.
     * <p>
     * Serialization of this message by using this message will include (in this order for each message part):
     * <ol>
     * <li>The color of each message part.</li>
     * <li>The applicable stylizations for each message part.</li>
     * <li>The core text of the message part.</li>
     * </ol>
     * The primary omissions are tooltips and clickable actions. Consequently, this method should be used only as a last resort.
     * </p>
     * <p>
     * Color and formatting can be removed from the returned string by using {@link ChatColor#stripColor(String)}.
     * </p>
     *
     * @return A human-readable string representing limited formatting in addition to the core text of this message.
     */
    public String toOldMessageFormat() {
        return BaseComponent.toLegacyText(builder.create());
    }

    /**
     * Set the behavior of the current editing component to display raw text when the client hovers over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param lines The lines of text which will be displayed to the client upon hovering. The iteration order of this object will
     *              be the order in which the lines of the tooltip are created.
     * @return This builder instance.
     */
    public FancyMessage tooltip(final Collection<String> lines) {
        tooltip(String.join("\n", lines));
        return this;
    }

    /**
     * Set the behavior of the current editing component to display raw text when the client hovers over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param text The text, which supports newlines, which will be displayed to the client upon hovering.
     * @return This builder instance.
     */
    public FancyMessage tooltip(final String text) {
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(text))));
        return this;
    }

    /**
     * Set the behavior of the current editing component to display raw text when the client hovers over the text.
     * <p>
     * Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are
     * applied.
     * </p>
     *
     * @param lines The lines of text which will be displayed to the client upon hovering.
     * @return This builder instance.
     */
    public FancyMessage tooltip(final String... lines) {
        tooltip(String.join("\n", lines));
        return this;
    }

    /**
     * If the text is a translatable key, and it has replaceable values, this function can be used to set the replacements that
     * will be used in the message.
     *
     * @param replacements The replacements, in order, that will be used in the language-specific message.
     * @return This builder instance.
     */
    public FancyMessage translationReplacements(final Collection<FancyMessage> replacements) {
        if (builder.getCurrentComponent() instanceof TranslatableComponent) {
            ((TranslatableComponent) builder.getCurrentComponent()).setWith(
                    replacements.stream()
                            .map(message -> message.builder.create())
                            .flatMap(Arrays::<BaseComponent>stream)
                            .collect(Collectors.toList())
            );
        }
        return this;
    }

    /**
     * If the text is a translatable key, and it has replaceable values, this function can be used to set the replacements that
     * will be used in the message.
     *
     * @param replacements The replacements, in order, that will be used in the language-specific message.
     * @return This builder instance.
     */
    public FancyMessage translationReplacements(final FancyMessage... replacements) {
        if (builder.getCurrentComponent() instanceof TranslatableComponent) {
            ((TranslatableComponent) builder.getCurrentComponent()).setWith(
                    Arrays.stream(replacements)
                            .map(message -> message.builder.create())
                            .flatMap(Arrays::stream)
                            .collect(Collectors.toList())
            );
        }
        return this;
    }

    /**
     * If the text is a translatable key, and it has replaceable values, this function can be used to set the replacements that
     * will be used in the message.
     *
     * @param replacements The replacements, in order, that will be used in the language-specific message.
     * @return This builder instance.
     */
    public FancyMessage translationReplacements(final String... replacements) {
        if (builder.getCurrentComponent() instanceof TranslatableComponent) {
            ((TranslatableComponent) builder.getCurrentComponent()).setWith(
                    Arrays.stream(replacements)
                            .map(TextComponent::fromLegacyText)
                            .flatMap(Arrays::stream)
                            .collect(Collectors.toList())
            );
        }
        return this;
    }
    /*
    
    /**
     * If the text is a translatable key, and it has replaceable values, this function can be used to set the replacements that will be used in the message.
     * @param replacements The replacements, in order, that will be used in the language-specific message.
     * @return This builder instance.
     */ /* ------------
        public FancyMessage translationReplacements(final Iterable<? extends CharSequence> replacements){
        for(CharSequence str : replacements){
        latest().translationReplacements.add(new JsonString(str));
        }
        
        return this;
        }
        
        */

}
