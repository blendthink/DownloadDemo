package dev.honwaka_lab.downloaddemo

import androidx.lifecycle.ViewModel

class MainViewModel(private val downloadService: DownloadService) : ViewModel() {

    fun onDownload() {
        downloadService.download(
            "https://www.kantei.go.jp/jp/singi/it2/senmon_bunka/data_ryutsuseibi/jititai_swg_dai4/siryou2-2.pdf",
            "siryou2-2.pdf"
        )
    }
}