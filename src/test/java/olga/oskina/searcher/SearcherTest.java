package olga.oskina.searcher;

import olga.oskina.indexer.Indexer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SearcherTest {

	private final String testString1 = "Владелец сайта — американская некоммерческая организация «Фонд Викимедиа», " +
			"имеющая 39 региональных представительств. Название энциклопедии образовано от английских слов wiki " +
			"(вики, технология, лежащая в основе функционирования сайта; в свою очередь заимствовано из гавайского" +
			" языка, в котором оно имеет значение «быстро») и encyclopedia (энциклопедия).";

	private final String testString2 = "Название энциклопедии образовано от английских слов wiki " +
			"(вики, технология, лежащая в основе функционирования сайта; в свою очередь заимствовано из гавайского" +
			" языка, в котором оно имеет значение «быстро») и encyclopedia (энциклопедия).";

	private final String testString3 = "Википедия создаётся добровольцами со всего мира на 276 мировых языках, а " +
			"также на 493 языках в инкубаторе. Она содержит более 30 миллионов статей. Интернет-сайт Википедии" +
			" является пятым по посещаемости сайтом в мире — в марте 2013 года его посетили более 517 миллионов" +
			" человек. Запущенная в январе 2001 года Джимми Уэйлсом и Ларри Сэнгером Википедия сейчас является" +
			" самым крупным и наиболее популярным справочником в Интернете.";

	private final String[] requests = new String[]{
			"язык /30 сайт",
			
			"энциклопедии /1 образовано",
			"энциклопедия /+1 образована",
			"функционирования /2 основа /+10 заимствовано /-3 в",

			"некоммерческая /1 американская /+1 некоммерческая",
			"Владелец",

			"энциклопедии /-1 образовано",
			"добровольцами /+1000 функционирование",
			"некоммерческая /1 американская /+1 организация",
			"лежащая /+4 основа /+5 из",

			"оно /123 ",
			"оно /123 функционирования encyclopedia",
	};

	private final String[] answers = new String[] {
			"tmp/tmp3.txt, tmp/tmp2.txt and 1 more",
			"tmp/tmp2.txt, tmp/tmp1.txt",
			"tmp/tmp2.txt, tmp/tmp1.txt",
			"tmp/tmp2.txt, tmp/tmp1.txt",
			"tmp/tmp1.txt",
			"tmp/tmp1.txt",
			"no documents found",
			"no documents found",
			"no documents found",
			"no documents found",
			"incorrect query",
			"incorrect query"
	};

	@Test
	public void runTest() {
		PropertyConfigurator.configure("log4g.properties");
		File folder = new File("tmp");
		File file1 = new File("tmp/tmp1.txt");
		File file2 = new File("tmp/tmp2.txt");
		File file3 = new File("tmp/tmp3.txt");
		final File indexFile = new File("index.inv");

		FileWriter fileWriter;
		try {
			if (folder.mkdir() && file1.createNewFile() && file2.createNewFile() && file3.createNewFile()) {
				fileWriter = new FileWriter(file1);
				fileWriter.append(testString1);
				fileWriter.close();
				fileWriter = new FileWriter(file2);
				fileWriter.append(testString2);
				fileWriter.close();
				fileWriter = new FileWriter(file3);
				fileWriter.append(testString3);
				fileWriter.close();
				Indexer indexer = new Indexer(folder, indexFile, Logger.getLogger(getClass()));
				indexer.work();
				Searcher searcher = new Searcher(indexFile);

				for (int i = 0; i < requests.length; i++) {
					final String answer = searcher.find(requests[i]);
					System.out.println(answer);
					assert (answer.equals(answers[i]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			file1.delete();
			file2.delete();
			file3.delete();
			folder.delete();
		}
	}

}