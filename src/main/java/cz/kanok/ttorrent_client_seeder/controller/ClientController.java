package cz.kanok.ttorrent_client_seeder.controller;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

@RestController
public class ClientController {

	private static final Logger logger = LoggerFactory.getLogger(ClientController.class.getName());

	//created by
	private static final String CREATED_BY = "createdByVitezslavKanok";
	//URI to Tracker
	private static final String TRACKER_URI = "http://0.0.0.0:6969/announce";
	//.torrent extension
	private static final String TORRENT_EXTENSION = ".torrent";
	//Path to shared file
	private static final String PATH_TO_SHARED_FILE = "c:/Ttorrent/ClientSeeder";
	//file for share name
	private static final String SHARED_FILE_NAME = "bcprace.pdf";
	//PATH to .torrent file
	private static final String PATH_TO_TORRENT_FILE = "c:/Ttorrent/Torrent";

	//DEFAULT SEEDER PORT 8095
	private Client seeder;
	//.torrent file name generated in createTorrentFile
	private String torrentFileName = "";

	@GetMapping("create-torrent")
	public void createTorrentFile() {
		try {
			logger.info("create new .torrent metainfo file...");
			Torrent torrent = Torrent.create(new File(PATH_TO_SHARED_FILE + File.separator + SHARED_FILE_NAME), new URI(TRACKER_URI), CREATED_BY);
			logger.info("Seed torrent: {}", torrent.isSeeder());
			logger.info("save .torrent to file...");
			torrentFileName = FilenameUtils.removeExtension(torrent.getName()) + TORRENT_EXTENSION;
			FileOutputStream fos = new FileOutputStream(PATH_TO_TORRENT_FILE + File.separator + torrentFileName);
			torrent.save(fos);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("start-client")
	public void startClient() throws IOException, NoSuchAlgorithmException {
		if (torrentFileName == null) {
			logger.info("Torrent file name is NULL");
		} else {
			// First, instantiate the Client object.
			this.seeder = new Client(
					// This is the interface the client will listen on (you might need something
					// else than localhost here).
					InetAddress.getLocalHost(),

					// Load the torrent from the torrent file and use the given
					// output directory. Partials downloads are automatically recovered.
					SharedTorrent.fromFile(
							new File(PATH_TO_TORRENT_FILE + File.separator + torrentFileName),
							new File(PATH_TO_SHARED_FILE)));

			// You can optionally set download/upload rate limits
			// in kB/second. Setting a limit to 0.0 disables rate
			// limits.
			seeder.setMaxDownloadRate(50.0);
			seeder.setMaxUploadRate(50.0);

			// At this point, can you either call download() to download the torrent and
			// stop immediately after...
			//seeder.download();

			// Or call client.share(...) with a seed time in seconds:
			seeder.share(3600);
			// Which would seed the torrent for an hour after the download is complete.

			// Downloading and seeding is done in background threads.
			// To wait for this process to finish, call:
			seeder.waitForCompletion();
		}

	}

	@GetMapping("stop-client")
	public void stopClient() {
		// At any time you can call client.stop() to interrupt the download.
		this.seeder.stop();
	}
}
