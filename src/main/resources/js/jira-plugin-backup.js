AJS.toInit(function () {
    var restUrl = PLUGIN_BACKUP_REST_URL;
    var table = AJS.$("#plugin-backup-table");
    var tbody = AJS.$("#plugin-backup-tbody");
    var loading = AJS.$("#plugin-backup-loading");
    var message = AJS.$("#plugin-backup-message");
    var downloadAllBtn = AJS.$("#download-all-btn");

    function formatSize(bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return (bytes / 1024).toFixed(1) + " KB";
        return (bytes / 1048576).toFixed(1) + " MB";
    }

    function showError(text) {
        message.removeClass("aui-message-info").addClass("aui-message-error")
            .html("<p>" + text + "</p>").show();
    }

    AJS.$.ajax({
        url: restUrl + "/list",
        type: "GET",
        dataType: "json",
        success: function (data) {
            loading.hide();

            if (!data || data.length === 0) {
                message.html("<p>No user-installed plugins found.</p>").show();
                return;
            }

            for (var i = 0; i < data.length; i++) {
                var plugin = data[i];
                var row = AJS.$("<tr></tr>");
                row.append("<td>" + AJS.$("<span>").text(plugin.name).html() + "</td>");
                row.append("<td>" + formatSize(plugin.size) + "</td>");
                row.append("<td>" + AJS.$("<span>").text(plugin.modified).html() + "</td>");
                row.append('<td><a href="' + restUrl + '/download?name=' +
                    encodeURIComponent(plugin.name) + '" class="aui-button aui-button-compact">Download</a></td>');
                tbody.append(row);
            }

            table.show();
            downloadAllBtn.prop("disabled", false);
        },
        error: function () {
            loading.hide();
            showError("Failed to load plugin list.");
        }
    });

    downloadAllBtn.on("click", function () {
        window.location.href = restUrl + "/download-all";
    });
});
