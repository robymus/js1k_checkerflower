#!/usr/bin/groovy


/* 
 * this is absolutely _not_ optimized, slow, probably will not work for all of the scripts
 * it's fine however for my current project
 *
 * based on Javascript crusher by @aivopaas. (http://www.iteral.com/jscrush/)
 * the decoder is reused as it is.
 * encoder works, somehow, but really slow, doing a lot of redundant and slow searches, too tired to fix :)
 */

import java.util.regex.Pattern

/** reimplemented compression, returns two strings: the compressed js, and the key (characters to replace) */
def crush(inputjs) {
	// get available keys (not used characters)
	availableKeys = new String((1..127) as byte[]).getChars() as Collection;
	availableKeys -= inputjs.getChars() as Collection;
	availableKeys -= "\\'\r\n".getChars() as Collection;

	// working values for compression
	str = inputjs.replaceAll(/[\r\n]/, '').replaceAll(/'/, '"'); // this is cheating, ' gets replaced to "
	keylist = "";

	while (availableKeys.size > 0) {
		// we have keys available, try to find something to compress

		System.err.print "."

		best = 0;
		len = str.length();
		maxlen = (int)(len/2)
		(maxlen..2).each() { blocklen ->
			maxidx = len-2*blocklen
			(0..(len-2*blocklen)).each() { blockstart ->
				block = str[blockstart..<(blockstart+blocklen)]
				cnt = str.findAll(Pattern.quote(block));
				gain = (blocklen-1)*cnt.size - blocklen - 1;
				if (gain > best || best == 0) {
					best = gain;
					beststr = block;
				}
			}
		}

		if (best <= 0) break; // no more match


		key = availableKeys[0];
		availableKeys -= key;
		key = (String) key;
		str = str.replace(beststr, key)+key+beststr;
		keylist = keylist + key;
	}

	return [str, keylist]
}

def createDecompressor(js, key)
{
	decompressor = sprintf("_='%s';for(Y=0;\$='%s'[Y++];)with(_.split(\$))_=join(pop());eval(_);",
	js, key.reverse());

	System.err.println(decompressor.length());
	return decompressor;
}

input = System.in.text
System.err.print(input.length())

(js,key) = crush(input);

print createDecompressor(js, key);
