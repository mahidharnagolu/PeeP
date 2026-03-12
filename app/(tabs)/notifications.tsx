import { View, Text, StyleSheet } from 'react-native';
import { Bell } from 'lucide-react-native';

export default function NotificationsScreen() {
    return (
        <View style={styles.container}>
            {/* BeReal-style centered header */}
            <View style={styles.header}>
                <Text style={styles.logo}>PeeP.</Text>
            </View>

            <View style={styles.content}>
                <Bell color="#666666" size={48} strokeWidth={1.5} />
                <Text style={styles.emptyTitle}>No notifications yet</Text>
                <Text style={styles.emptySubtitle}>
                    When someone peeps you, it'll show up here.
                </Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#000000',
    },
    header: {
        paddingTop: 56,
        paddingBottom: 16,
        alignItems: 'center',
    },
    logo: {
        color: '#FFFFFF',
        fontSize: 26,
        fontWeight: '800',
        letterSpacing: 0.5,
    },
    content: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 40,
    },
    emptyTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#FFFFFF',
        marginTop: 20,
        textAlign: 'center',
    },
    emptySubtitle: {
        fontSize: 14,
        color: '#999999',
        marginTop: 8,
        textAlign: 'center',
        lineHeight: 20,
    },
});
