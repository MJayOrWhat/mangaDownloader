package de.jando.manga

import de.jando.manga.download.HttpClient

abstract class MangaTube {

    public void beginWithDownload() {
        this.setupFolder()
        httpClient = new HttpClient(path: path)
        this.getFileListToDownload()
    }

    void setupFolder() {
        new File(path + folderAndFileName).mkdirs()
    }

    public boolean fileAlreadyExists(String episodeNumber) {
        return httpClient.checkForFile(episodeNumber, folderAndFileName)
    }

    void getFileListToDownload() {
        String allEpisodeHtmlContent = httpClient.getFileContentString(allEpisodes)
        extractAllEpisodeNumbers(allEpisodeHtmlContent)
        loadAllVideoPages()
    }

    void extractAllEpisodeNumbers(String alleEpisodeHtmlContent) {
        println "Fetching all EpisodeNumbers"
        alleEpisodeHtmlContent.eachLine { String htmlLine ->
            if (htmlLine.contains(this.htmlLine)) {
                episodeList.add(htmlLine.split("-")[indexForEpisode].replace("'", "").replace("\"", "").replace(">", ""))
            }
        }
    }

    void loadAllVideoPages() {
        if (!fileAlreadyExists(episodeNumber)) {
            episodeList.each { String episodeNumber ->
                println "#####################################"
                println "Now at Epsiodenumber ${episodeNumber}"
                String videoPageContent = httpClient.getFileContentString(completeUrl + episodeNumber)
                filterVideoPage(videoPageContent, episodeNumber)
            }
        }else {
            println "${folderAndFileName} Episode ${episodeNumber} skipped, it already exist"
        }
    }

    void filterVideoPage(String videoPageContent, String episodeNumber) {
        videoPageContent.eachLine { String htmlLine ->
            String videoUrl = null
            if (htmlLine.contains("ani-stream")) {
                println "Video file fetched"
                videoUrl = htmlLine.split("\"")[1]
            }
            if (videoUrl)
                fetchVideoFromPage(videoUrl, episodeNumber)
        }
    }

    void fetchVideoFromPage(String videoPageUrl, String episodeNumber) {
        String videoPageContent = httpClient.getFileContentString(videoPageUrl)
        videoPageContent.eachLine { String htmlLine ->
            if (htmlLine.contains("file: ") && htmlLine.endsWith(",")) {
                downloadVideo(getVideoUrl(htmlLine), episodeNumber)
            }
        }
    }

    String getVideoUrl(String htmlLine) {
        return htmlLine.split("file: ")[1].replace("'", "").replace(",", "")
    }

    void downloadVideo(String videoFile, String episodeNumber) {
        httpClient.downloadFileResource(videoFile, folderAndFileName, episodeNumber)
    }

}
