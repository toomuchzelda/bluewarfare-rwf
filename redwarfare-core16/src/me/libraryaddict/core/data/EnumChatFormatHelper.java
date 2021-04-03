package me.libraryaddict.core.data;

import org.bukkit.Bukkit;

import net.minecraft.server.v1_16_R3.EnumChatFormat;

public class EnumChatFormatHelper {

	//this method relies on the String colorCode being a minecraft color code like §b
	//and only being used by setPrefix
	//this is very ugly
	public static EnumChatFormat enumChatFromString(String colorCode) {
		
		int length = colorCode.length();
		
		if(length <= 0)
		{
			return EnumChatFormat.RESET;
		}
		
		char one = colorCode.charAt(0);
		
		if(length > 0)
		{
			if(one != '§')
			{
				return EnumChatFormat.RESET;
			}
		}
		
		if(length == 1 && one == '§')
		{
			return EnumChatFormat.RESET;
		}
		
		char c = colorCode.charAt(1);
		
		EnumChatFormat enumChat;
		
		switch(c)
		{
		case '0':
			enumChat = EnumChatFormat.BLACK;
			break;
		case '1':
			enumChat = EnumChatFormat.DARK_BLUE;
			break;
		case '2':
			enumChat = EnumChatFormat.DARK_GREEN;
			break;
		case '3':
			enumChat = EnumChatFormat.DARK_AQUA;
			break;
		case '4':
			enumChat = EnumChatFormat.DARK_RED;
			break;
		case '5':
			enumChat = EnumChatFormat.DARK_PURPLE;
			break;
		case '6':
			enumChat = EnumChatFormat.GOLD;
			break;
		case '7':
			enumChat = EnumChatFormat.GRAY;
			break;
		case '8':
			enumChat = EnumChatFormat.DARK_GRAY;
			break;
		case '9':
			enumChat = EnumChatFormat.BLUE;
			break;
		case 'a':
			enumChat = EnumChatFormat.GREEN;
			break;
		case 'b':
			enumChat = EnumChatFormat.AQUA;
			break;
		case 'c':
			enumChat = EnumChatFormat.RED;
			break;
		case 'd':
			enumChat = EnumChatFormat.LIGHT_PURPLE;
			break;
		case 'e':
			enumChat = EnumChatFormat.YELLOW;
			break;
		case 'f':
			enumChat = EnumChatFormat.WHITE;
			break;
		case 'k':
			enumChat = EnumChatFormat.OBFUSCATED;
			break;
		case 'l':
			enumChat = EnumChatFormat.BOLD;
			break;
		case 'm':
			enumChat = EnumChatFormat.STRIKETHROUGH;
			break;
		case 'n':
			enumChat = EnumChatFormat.UNDERLINE;
			break;
		case 'o':
			enumChat = EnumChatFormat.ITALIC;
			break;
		case 'r':
			enumChat = EnumChatFormat.RESET;
			break;
		default:
			enumChat = EnumChatFormat.RESET;
		}
		
		return enumChat;
		
	}
	
}
