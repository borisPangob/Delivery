const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendNotificationOnNewMission =
functions.firestore.document("mission/{missionId}")
    .onCreate((snapshot, context) => {
      const missionData = snapshot.data();

      // Vérifiez si la mission a été créée avec succès
      if (!missionData) {
        return null;
      }

      // Construisez le payload de la notification
      const payload = {
        notification: {
          title: "Nouvelle mission disponible",
          body: "Une nouvelle mission est en attente de validation !",
        },
        data: {
          missionId: context.params.missionId,
        },
      };

      // Envoyez la notification aux utilisateurs abonnés au topic
      // "nouvelle_mission"
      return admin.messaging().sendToTopic("nouvelle_mission", payload);
    });
