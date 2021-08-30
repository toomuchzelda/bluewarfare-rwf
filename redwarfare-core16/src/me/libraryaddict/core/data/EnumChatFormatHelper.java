package me.libraryaddict.core.data;

//import net.minecraft.server.v1_16_R3.EnumChatFormat;

import net.minecraft.ChatFormatting;

public class EnumChatFormatHelper {

	//this method relies on the String colorCode being a minecraft color code like §b
	//and only being used by setPrefix
	//this is very ugly
	public static ChatFormatting enumChatFromString(String colorCode) {
		
		int length = colorCode.length();
		
		if(length <= 0)
		{
			return ChatFormatting.RESET;
		}
		
		char one = colorCode.charAt(0);
		
		if(length > 0)
		{
			if(one != '§')
			{
				return ChatFormatting.RESET;
			}
		}
		
		if(length == 1 && one == '§')
		{
			return ChatFormatting.RESET;
		}
		
		char c = colorCode.charAt(1);
		
		ChatFormatting enumChat;
		
		switch(c)
		{
		case '0':
			enumChat = ChatFormatting.BLACK;
			break;
		case '1':
			enumChat = ChatFormatting.DARK_BLUE;
			break;
		case '2':
			enumChat = ChatFormatting.DARK_GREEN;
			break;
		case '3':
			enumChat = ChatFormatting.DARK_AQUA;
			break;
		case '4':
			enumChat = ChatFormatting.DARK_RED;
			break;
		case '5':
			enumChat = ChatFormatting.DARK_PURPLE;
			break;
		case '6':
			enumChat = ChatFormatting.GOLD;
			break;
		case '7':
			enumChat = ChatFormatting.GRAY;
			break;
		case '8':
			enumChat = ChatFormatting.DARK_GRAY;
			break;
		case '9':
			enumChat = ChatFormatting.BLUE;
			break;
		case 'a':
			enumChat = ChatFormatting.GREEN;
			break;
		case 'b':
			enumChat = ChatFormatting.AQUA;
			break;
		case 'c':
			enumChat = ChatFormatting.RED;
			break;
		case 'd':
			enumChat = ChatFormatting.LIGHT_PURPLE;
			break;
		case 'e':
			enumChat = ChatFormatting.YELLOW;
			break;
		case 'f':
			enumChat = ChatFormatting.WHITE;
			break;
		case 'k':
			enumChat = ChatFormatting.OBFUSCATED;
			break;
		case 'l':
			enumChat = ChatFormatting.BOLD;
			break;
		case 'm':
			enumChat = ChatFormatting.STRIKETHROUGH;
			break;
		case 'n':
			enumChat = ChatFormatting.UNDERLINE;
			break;
		case 'o':
			enumChat = ChatFormatting.ITALIC;
			break;
		case 'r':
			enumChat = ChatFormatting.RESET;
			break;
		default:
			enumChat = ChatFormatting.RESET;
		}
		
		return enumChat;
		
	}
	
}
